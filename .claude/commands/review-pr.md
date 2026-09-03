---
description: Roda a revisão da fábrica (ac-reviewer + error-scanner), gera o commit e abre o MR.
argument-hint: [branch-alvo, default main]
---
1. Rode os agentes `ac-reviewer` e `error-scanner` contra o diff atual vs a branch alvo
   (`$ARGUMENTS`, default `main`).
2. Se algum achado for bloqueante, pare e liste — não commite código que não passou na revisão.
3. Se limpo, gere uma mensagem de commit em Conventional Commits a partir do diff e do critério de
   aceite que ele resolve, e rode `git push -u origin HEAD`.
4. Abra o Merge Request no GitLab. **Sem o MCP do GitLab configurado (isso entra no Dia 3), pare
   aqui e imprima o link `.../merge_requests/new` com a branch já pronta** — não invente uma
   chamada de API que você não tem como executar.
5. Se a tarefa tiver uma referência de task do ClickUp (no branch name, no commit, ou porque o
   usuário mencionou uma), use as tools do MCP do ClickUp pra comentar nela: link do diff/MR e um
   resumo curto do que mudou. Se não houver task referenciada, pule este passo sem perguntar.

Nunca use `git push --force` nem commite direto em branch protegida (ver `.claude/rules/RULES.md`).
