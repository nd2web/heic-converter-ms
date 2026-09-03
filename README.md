# heic-converter-ms (gabarito)

Microserviço Spring Boot criado do zero como exemplo do Dia 1 — converte HEIC/HEIF para JPEG
usando `heif-convert` (libheif). **Este é o gabarito de referência** para o facilitador; a turma
constrói o seu próprio a partir da spec (`specs/heic-converter-ms.md`), em
`../ponto-de-partida/`.

Este gabarito também cobre a spec `specs/documentacao-openapi-swagger.md` (documentação viva
via springdoc-openapi), usada para validar a implementação antes de liberar o exercício.

## Rodar
```bash
# pré-requisito: heif-convert instalado (brew install libheif / apt install libheif-examples)
mvn spring-boot:run
# converter uma imagem:
curl -F "file=@foto.heic" http://localhost:8080/api/v1/convert -o convertido.jpg
```

## Testar
```bash
mvn -q test
```

## Docker
```bash
docker build -t heic-converter-ms . && docker run -p 8080:8080 heic-converter-ms
```

## Endpoints
- `POST /api/v1/convert` (multipart `file`) → JPEG.
- `GET /actuator/health`, `GET /actuator/prometheus`.
- `GET /v3/api-docs`, `GET /swagger-ui.html` — documentação OpenAPI/Swagger.
