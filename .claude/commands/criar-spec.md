---
description: Gera uma spec em specs/, fazendo perguntas direcionadas ao usuário antes de escrever.
argument-hint: [nome-da-feature]
---
Você vai criar `specs/$ARGUMENTS.md` (spec-driven, conforme `CLAUDE.md`). Se `$ARGUMENTS` estiver
vazio, pergunte primeiro o nome/slug da feature.

Antes de escrever qualquer linha da spec, faça perguntas direcionadas ao usuário (uma pergunta por
vez, com opções quando fizer sentido) até ter clareza sobre:
1. **Objetivo** — que problema resolve, quem consome.
2. **Comportamento esperado** — endpoints/comandos, formato de entrada e saída.
3. **Stack** — confirmar se segue o padrão da casa (Java 21 + Spring Boot + Maven) ou diverge, e por quê.
4. **Erros e limites** — o que pode falhar, como deve responder, timeouts/limites de tamanho.
5. **Critérios de aceite** — cenários testáveis em BDD (Dado/Quando/Então).
6. **Fora de escopo** — o que explicitamente não entra nesta versão.
7. **Segurança/LGPD** — dados sensíveis envolvidos, se algum, conforme `.claude/rules/RULES.md`.

Não pergunte o que já está claro no pedido do usuário ou nas convenções do projeto — só o que
realmente muda o design. Depois de reunir as respostas, escreva a spec em `specs/$ARGUMENTS.md`
seguindo a mesma estrutura de `specs/heic-converter-ms.md`: Objetivo, Stack, Comportamento
esperado, Requisitos de saída, Critérios de aceite (BDD), Plano de testes, Fora de escopo, Notas
de ambiente (se aplicável).

Apresente a spec gerada para o usuário confirmar. Não implemente código nesta etapa — só a spec.
