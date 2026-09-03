---
description: Implementa/gera o serviço a partir de uma spec, com plano, testes e validação.
argument-hint: [caminho-da-spec]
---
Leia a spec em `$ARGUMENTS`, o `CLAUDE.md` e `.claude/rules/RULES.md`.
1. Apresente um plano curto antes de gerar código (plan mode).
2. Faça o bootstrap do projeto se ele ainda não existir (pom, Application, pacote com.unimed.heic).
3. Implemente cobrindo cada critério de aceite com testes.
4. Rode `mvn -q test` até passar. Não conclua com testes falhando.
5. Resuma o que foi criado e como rodar.
