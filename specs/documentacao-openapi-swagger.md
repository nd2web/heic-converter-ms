# Spec: documentacao-openapi-swagger

Expor a documentação viva (OpenAPI/Swagger) do `heic-converter-ms`, gerada automaticamente a
partir dos controllers já existentes — sem YAML manual, sem anotações extensas nesta versão.

## Objetivo
Dar visibilidade ao contrato da API (`POST /api/v1/convert`) para quem for integrar com o
serviço — outros times/serviços do fluxo de guias e QA — sem depender de documentação escrita à
mão que desatualiza. A UI e o JSON do OpenAPI devem refletir sempre o código atual.

## Stack (alinhada ao padrão da casa)
- Java 21, Spring Boot 3.3.x, Maven — sem mudança de stack.
- Biblioteca: `springdoc-openapi-starter-webmvc-ui` (versão compatível com Spring Boot 3.3.x,
  ex.: `2.6.0`) — gera a UI e o JSON automaticamente a partir dos controllers/`MultipartFile`
  existentes, sem exigir um YAML mantido à mão.

## Comportamento esperado
- `GET /v3/api-docs` → responde `200` com o JSON do OpenAPI (versão `3.x`), incluindo o path
  `/api/v1/convert` com método `POST`, `consumes: multipart/form-data` e as respostas `200`
  (`image/jpeg`) e `422`.
- `GET /swagger-ui.html` (redireciona para `/swagger-ui/index.html`) → responde `200` com a UI
  HTML do Swagger.
- Disponível em **todos os ambientes**, sem toggle por profile — é um serviço interno sem dados
  sensíveis expostos via metadados de documentação.
- Metadados da API (título, descrição, versão) são derivados automaticamente do `pom.xml`
  (`artifactId`/`description`/`version`) — sem valores hardcoded a manter.
- Endpoints do Actuator (`/actuator/**`) **não** entram na documentação OpenAPI (ferramenta
  própria de observabilidade, fora do escopo do springdoc).

## Requisitos de saída
- `GET /v3/api-docs` → `200`, `Content-Type: application/json`, corpo é OpenAPI 3.x válido.
- `GET /swagger-ui.html` → `200`, `Content-Type: text/html`.
- Nenhuma mudança de comportamento nos endpoints existentes (`/api/v1/convert`,
  `/actuator/health`, `/actuator/prometheus`).

## Critérios de aceite (testáveis, em BDD)
- [ ] **Dado** o serviço no ar, **quando** `GET /v3/api-docs` é chamado, **então** a resposta é
      `200` e o JSON contém o path `/api/v1/convert`.
- [ ] **Dado** o serviço no ar, **quando** `GET /swagger-ui.html` é chamado, **então** a resposta
      é `200` (após redirecionamento) com HTML da UI do Swagger.
- [ ] **Dado** o spec OpenAPI gerado, **quando** inspecionado o path `/api/v1/convert`, **então**
      ele declara `consumes: multipart/form-data` e as respostas `200` e `422`.
- [ ] **Dado** o serviço rodando com a configuração padrão (sem profile especial), **quando**
      qualquer um dos dois endpoints acima é chamado, **então** ambos respondem sem exigir
      configuração adicional (documentação sempre ligada).

## Plano de testes
- Integração (`@SpringBootTest` + `MockMvc`/`TestRestTemplate`, mesmo padrão dos testes de
  controller já existentes):
  - `GET /v3/api-docs` → `200` + JSON parseável contendo a chave `/api/v1/convert`.
  - `GET /swagger-ui.html` → `200` (seguindo redirect) + `Content-Type` HTML.

## Fora de escopo
- Autenticação/autorização na UI ou no JSON do OpenAPI (o MS continua sem auth, conforme
  `.claude/CLAUDE.md`).
- Anotações detalhadas por endpoint (`@Operation`, `@ApiResponse`, exemplos de request/response
  campo a campo) — fica para uma iteração futura, se necessário.
- Versionamento de múltiplas versões de API (só existe `/api/v1` hoje).
- Publicação do spec em portal centralizado ou geração de SDK/client a partir do OpenAPI.

## Notas de ambiente
- Não introduz dependência de binário externo nem muda o `Dockerfile` — `springdoc-openapi` é
  apenas uma dependência Maven a mais no `pom.xml` existente.
