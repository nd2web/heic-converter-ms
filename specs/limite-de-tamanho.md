# Spec: limite-de-tamanho

Valida o tamanho de uploads antes de invocar o `heif-convert`, recusando arquivos acima de um
limite configurável com uma resposta HTTP 413 legível — sem gastar tempo de processo externo
com uploads que já sabemos que não devem ser convertidos.

## Objetivo
Proteger o endpoint `POST /api/v1/convert` contra uploads grandes, evitando invocar o processo
externo `heif-convert` (custoso e sujeito a timeout) para arquivos que já excedem um limite de
negócio configurável.

## Stack
Segue o padrão já estabelecido em `specs/heic-converter-ms.md` (Java 21, Spring Boot 3.3.x,
Maven, JUnit 5 + AssertJ). Nenhuma dependência nova.

## Comportamento esperado
- Nova propriedade `heic.max-file-size` (tipo `DataSize`, default `15MB`), ao lado de
  `heic.convert-command` e `heic.timeout` em `HeicProperties`.
- A validação de tamanho ocorre **antes** de `HeicConversionService` invocar o `heif-convert`
  (usa `MultipartFile.getSize()` no controller, sem precisar ler o arquivo inteiro em memória
  para arquivos que serão recusados).
- Limite é **exclusivo**: um arquivo com tamanho **maior que** `heic.max-file-size` é recusado.
  Um arquivo exatamente igual ao limite é aceito.
- Resposta de recusa: `HTTP 413 Payload Too Large`, corpo em texto plano com mensagem legível
  (ex.: `"Arquivo excede o limite máximo de 15MB"`), seguindo o mesmo padrão do handler 422
  existente em `GlobalExceptionHandler` (corpo `String`, sem envelope JSON).
- **Camada de segurança do Spring:** `spring.servlet.multipart.max-file-size` /
  `max-request-size` (hoje `20MB`) permanece como teto físico acima do limite de negócio — não
  é reduzido para `15MB`. Ele deve continuar maior que `heic.max-file-size` para que nossa
  validação (com mensagem controlada) seja sempre a primeira a disparar em uso normal.
- Quando o próprio Spring rejeitar a requisição por estourar esse teto físico
  (`MaxUploadSizeExceededException` — ex.: alguém sobe um arquivo maior que os 20MB do
  container), a spec também cobre esse caso: um handler dedicado responde `413` com mensagem
  legível, consistente com a validação de negócio, em vez do erro padrão do Spring/Tomcat.
- Novo tipo de exceção de domínio `FileTooLargeException extends RuntimeException`, com
  mensagem legível (segue o padrão de `HeicConversionException`).

## Requisitos de saída
- Recusa por limite de negócio (`heic.max-file-size`): HTTP 413, corpo texto com o motivo.
- Recusa por teto físico do Spring: HTTP 413, corpo texto com o motivo.
- Arquivo dentro do limite: fluxo normal, inalterado (200 ou 422 conforme sucesso/falha da
  conversão).
- Em nenhum dos casos de recusa por tamanho o processo `heif-convert` é iniciado.

## Critérios de aceite (BDD)
- [ ] **Dado** `heic.max-file-size=15MB`, **quando** um arquivo de 16MB é enviado a
      `/api/v1/convert`, **então** a resposta é `413` com mensagem legível e o `heif-convert`
      **não** é invocado.
- [ ] **Dado** `heic.max-file-size=15MB`, **quando** um arquivo de exatamente 15MB é enviado,
      **então** a validação de tamanho passa (a requisição segue para a conversão normalmente).
- [ ] **Dado** `heic.max-file-size=15MB`, **quando** um arquivo de 10MB é enviado, **então** a
      validação de tamanho passa.
- [ ] **Dado** um valor customizado de `heic.max-file-size` (ex.: `5MB`) via configuração,
      **quando** um arquivo de 6MB é enviado, **então** a resposta é `413`.
- [ ] **Dado** um arquivo maior que `spring.servlet.multipart.max-file-size` (teto físico),
      **quando** enviado, **então** a resposta é `413` com mensagem legível (não o erro padrão
      do Spring/Tomcat).
- [ ] **Dado** um upload dentro de todos os limites, **quando** a conversão falha por outro
      motivo (ex.: HEIC inválido), **então** a resposta continua `422`, sem alteração do
      comportamento existente.

## Plano de testes
- Unitário: `FileTooLargeException` lançada quando tamanho > `heic.max-file-size`; não lançada
  quando tamanho == limite ou tamanho < limite.
- Unitário: `GlobalExceptionHandler` — `FileTooLargeException` → 413 + corpo com a mensagem;
  `MaxUploadSizeExceededException` → 413 + mensagem legível.
- Integração (`MockMvc` ou `WebTestClient`, multipart): upload de bytes dummy (não precisa ser
  HEIC válido, pois a validação de tamanho ocorre antes da conversão) acima do limite → 413,
  sem chamar `HeicConversionService`/processo externo (verificar via mock/spy).
- Integração: upload acima de `spring.servlet.multipart.max-file-size` → 413 com corpo
  customizado (não o HTML/erro padrão do Spring).
- Regressão: teste de sucesso existente (`foto-exame.heic` → JPEG) continua passando.

## Fora de escopo
- Limite diferenciado por tipo de cliente/rota — um único `heic.max-file-size` global.
- Compressão ou redimensionamento de imagens grandes antes da validação.
- Streaming/validação incremental durante o upload (a validação usa `getSize()`, que o
  multipart resolver já resolve antes do controller ser chamado).

## Segurança / LGPD
- Nenhum dado sensível novo é logado — a mensagem de erro e os logs (`WARN`) citam apenas
  tamanho do arquivo e limite configurado, nunca conteúdo do upload, seguindo
  `.claude/rules/RULES.md`.
