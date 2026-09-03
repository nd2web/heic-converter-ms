# Spec: heic-converter-ms (microserviço novo, do zero)

Microserviço que recebe uma imagem HEIC/HEIF e devolve o JPEG convertido. Criado do zero no
Dia 1 como exemplo de bootstrap + spec-driven com o Claude Code. Espelha o conversor real do
`unimedjp-classificacao-documentos-ms`, mas isolado num serviço próprio.

## Objetivo
Expor uma API HTTP que converte HEIC/HEIF → JPEG, para ser usada como etapa de normalização
por outros serviços do fluxo de guias.

## Stack (alinhada ao padrão da casa)
- Java 21, Spring Boot 3.3.x, Maven.
- Testes: JUnit 5 + AssertJ (spring-boot-starter-test).
- Observabilidade: Spring Actuator + Micrometer/Prometheus.
- Empacotamento: Dockerfile com `libheif-examples` (fornece `heif-convert`).

## Comportamento esperado
- `POST /api/v1/convert` recebe multipart `file` (a imagem HEIC) → responde `image/jpeg`.
- A conversão delega ao binário `heif-convert` (libheif) — não há decodificador HEIC maduro em
  Java puro. Converte apenas a **imagem primária** (HEIC multi-imagem fica fora de escopo).
- Erros de conversão respondem `422 Unprocessable Entity` com mensagem legível.
- Configuração via `heic.convert-command` (default `heif-convert`) e `heic.timeout` (default 20s).

## Requisitos de saída
- Sucesso: corpo `image/jpeg` (bytes começando com `FF D8 FF`), HTTP 200.
- Falha de conversão: HTTP 422 + texto do motivo.
- `GET /actuator/health` e `GET /actuator/prometheus` disponíveis.

## Critérios de aceite (testáveis, em BDD)
- [ ] **Dado** um HEIC válido (fixture `foto-exame.heic`), **quando** convertido, **então** o
      resultado é um JPEG válido (magic bytes `FF D8 FF`).
- [ ] **Dado** uma entrada vazia, **quando** a conversão é chamada, **então** um erro identificável
      é gerado **sem** invocar o processo externo.
- [ ] **Dado** o binário `heif-convert` ausente, **quando** a conversão é chamada, **então** falha
      de forma limpa (sem travar) e retorna 422.
- [ ] **Dado** um timeout configurado, **quando** a conversão excede esse tempo, **então** o
      processo é encerrado e o timeout é respeitado.
- [ ] **Dado** um upload multipart válido, **quando** enviado ao endpoint, **então** a resposta é
      `image/jpeg`.

## Plano de testes
- Unitário: entrada vazia → exceção sem processo.
- Unitário: `convert-command` inexistente → exceção "falha ao invocar", sem travar.
- Integração: fixture HEIC → JPEG válido (usar `Assumptions.assumeTrue` para pular se `heif-convert` não estiver instalado).

## Fora de escopo
- HEIC multi-imagem / Live Photos (só a imagem primária).
- Persistência, fila/Kafka, autenticação — este MS é uma etapa de conversão sem estado.
- Outros formatos (WebP, AVIF).

## Notas de ambiente
- Dev local (fora de Docker): instalar `heif-convert` (`brew install libheif` no macOS;
  `apt-get install libheif-examples` no Ubuntu/WSL).
- Em Docker, o binário já entra pela imagem (ver Dockerfile).
