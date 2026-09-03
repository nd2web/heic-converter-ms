# Spec: endpoint-metadados (RF2)

Novo endpoint que converte um HEIC para JPEG (reaproveitando o pipeline existente) mas devolve
apenas os metadados do resultado — largura, altura e tamanho em bytes — sem transferir os bytes
da imagem de volta ao cliente.

## Objetivo
Permitir que serviços consumidores obtenham dimensões e tamanho do JPEG que seria gerado a
partir de um HEIC, sem o custo de banda de baixar a imagem inteira quando só o metadado importa
(ex.: validação prévia, decisões de fluxo antes de buscar a imagem completa).

## Stack
Mesma stack do `heic-converter-ms.md` (Java 21, Spring Boot 3.3.x, Maven, JUnit 5 + AssertJ).
Leitura de dimensões via `javax.imageio.ImageIO` (parte do JDK, sem dependência nova) sobre os
bytes do JPEG já convertido — não sobre o HEIC original.

## Comportamento esperado
- `POST /api/v1/metadata` recebe multipart `file` (mesmo contrato de `/api/v1/convert`) →
  responde `application/json`.
- Internamente reaproveita `HeicConversionService.convert(byte[])` (mesmo pipeline do
  `heif-convert`, mesmos timeouts e validação de JPEG); a diferença é só na resposta HTTP, que
  descarta os bytes da imagem e devolve metadados.
- Reaproveita as mesmas validações e limites do `/api/v1/convert`:
  - `heic.max-file-size` (spec [[limite-de-tamanho]]) → `413` antes de invocar a conversão.
  - Arquivo vazio / `heif-convert` ausente / timeout / saída inválida → `422`, mesmas mensagens
    de `HeicConversionException`.
- Corpo da resposta (200):
  ```json
  {
    "largura": 4032,
    "altura": 3024,
    "tamanhoBytes": 812345
  }
  ```
  - `largura` / `altura`: dimensões em pixels do JPEG convertido (via `ImageIO`).
  - `tamanhoBytes`: tamanho em bytes do JPEG convertido (`jpegBytes.length`), **não** o tamanho
    do HEIC original enviado.
- Se `ImageIO` não conseguir ler as dimensões do JPEG gerado (caso extremo — a validação de
  magic bytes `FF D8 FF` já ocorre dentro de `HeicConversionService`, mas o arquivo pode estar
  truncado/corrompido de outra forma), responde `422` com mensagem legível — mesmo padrão dos
  demais erros de conversão.

## Requisitos de saída
- Sucesso: HTTP 200, `Content-Type: application/json`, corpo com `largura`, `altura`,
  `tamanhoBytes` (os três sempre presentes e não nulos).
- Falha de conversão: HTTP 422 + texto do motivo (igual a `/api/v1/convert`).
- Upload acima do limite: HTTP 413 + texto do motivo (igual a `/api/v1/convert`, spec
  [[limite-de-tamanho]]).
- A imagem convertida **nunca** é retornada no corpo desta resposta.

## Critérios de aceite (BDD)
- [ ] **Dado** um HEIC válido (fixture `foto-exame.heic`), **quando** enviado a
      `/api/v1/metadata`, **então** a resposta é `200`, `application/json`, com `largura` e
      `altura` iguais às dimensões reais do JPEG que `/api/v1/convert` geraria para o mesmo
      arquivo, e `tamanhoBytes` igual ao tamanho desse JPEG.
- [ ] **Dado** o mesmo HEIC válido, **quando** convertido via `/api/v1/metadata`, **então** o
      corpo da resposta **não** contém os bytes da imagem (não é `image/jpeg`).
- [ ] **Dado** uma entrada vazia, **quando** enviada a `/api/v1/metadata`, **então** a resposta
      é `422`, sem invocar o `heif-convert`.
- [ ] **Dado** um upload acima de `heic.max-file-size`, **quando** enviado a
      `/api/v1/metadata`, **então** a resposta é `413`, sem invocar o `heif-convert`.
- [ ] **Dado** o binário `heif-convert` ausente, **quando** chamado `/api/v1/metadata`,
      **então** a resposta é `422` (mesmo comportamento de `/api/v1/convert`).

## Plano de testes
- Unitário: novo componente (ex. `ImageMetadataService`/método no controller) que recebe bytes
  de JPEG válido e devolve largura/altura/tamanho corretos (usar uma imagem JPEG pequena de
  fixture com dimensões conhecidas).
- Unitário: bytes que não são uma imagem decodificável pelo `ImageIO` → `HeicConversionException`
  (ou exceção equivalente) → mapeada para `422`.
- Integração: fixture HEIC real → `POST /api/v1/metadata` → `200` + JSON com os 3 campos,
  valores consistentes com o JPEG gerado por `/api/v1/convert` para o mesmo arquivo (usar
  `Assumptions.assumeTrue` para pular se `heif-convert` não estiver instalado, mesmo padrão da
  spec de conversão).
- Integração: entrada vazia → `422`; upload acima do limite → `413` (reaproveita cenários já
  cobertos em `limite-de-tamanho.md`, agora também para esta rota).

## Fora de escopo
- Cache de metadados (cada chamada reconverte o HEIC do zero).
- Metadados EXIF/orientação, espaço de cor, ou qualquer campo além de largura/altura/tamanho.
- Suporte a HEIC multi-imagem/Live Photos (mesma limitação de `heic-converter-ms.md`: só a
  imagem primária).
- Persistência do resultado — resposta é stateless, como o resto do serviço.

## Segurança / LGPD
- Nenhum dado sensível novo: a resposta expõe apenas dimensões e tamanho em bytes, nunca o
  conteúdo da imagem. Segue `.claude/rules/RULES.md` (sem logar conteúdo de imagens/PII).
