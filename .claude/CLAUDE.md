# CLAUDE.md — heic-converter-ms (novo, do zero)

Microserviço de conversão HEIC → JPEG. Criado do zero no Dia 1 do bootcamp.

## Stack
- Java 21, Spring Boot 3.3.x, Maven.
- Testes: JUnit 5 + AssertJ (spring-boot-starter-test).
- Observabilidade: Actuator + Micrometer/Prometheus.
- Conversão: binário `heif-convert` (libheif) via ProcessBuilder.

## Comandos
- Build: `mvn -q -B package`
- Testes: `mvn -q test`
- Rodar: `mvn spring-boot:run`
- Converter: `curl -F "file=@recursos/foto-exame.heic" http://localhost:8080/api/v1/convert -o out.jpg`

## Convenções
- A spec em `specs/heic-converter-ms.md` é a fonte da verdade — comece por ela.
- Toda mudança de comportamento entra por spec + testes.
- Pacote base: `com.unimed.heic`.
- Siga `.claude/rules/RULES.md`.

## Fluxo de trabalho
1. Ler a spec e apresentar um plano curto antes de gerar código.
2. Fazer o bootstrap do projeto (pom, Application, estrutura) a partir da spec.
3. Implementar em pequenos passos, rodando os testes a cada ciclo.
4. Nunca concluir com testes falhando.

## Não fazer
- Não expor dados sensíveis/PII ao agente (LGPD).
- Não sair do escopo da spec (sem persistência, fila ou auth nesta versão).
