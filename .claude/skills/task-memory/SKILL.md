---
name: task-memory
description: Salva em .claude/memory um registro do escopo trabalhado na tarefa, com ticket do ClickUp, decisões tomadas, data e responsáveis. Use ao final do trabalho (antes ou depois do MR).
---
Escreva um arquivo novo em `.claude/memory/AAAA-MM-DD-<slug-da-tarefa>.md` (slug a partir do nome
da branch ou da spec trabalhada) com esta estrutura:

```
# <Título curto da tarefa>

- **Data:** <data de hoje>
- **Ticket ClickUp:** <link/ID, ou "não aplicável">
- **Responsáveis:** <autor do commit (git config user.name/email)> + Claude Code

## Escopo trabalhado
<o que foi implementado/alterado — endpoints, classes, specs — 3-6 linhas>

## Decisões tomadas
<lista das decisões técnicas relevantes e o porquê — trade-offs, alternativas descartadas>

## Status
<testes rodando? algo pendente/fora de escopo?>
```

Como preencher cada campo:
1. **Ticket ClickUp** — procure referência no nome da branch, nas mensagens de commit do diff atual
   ou porque o usuário mencionou uma nesta conversa. Se o MCP do ClickUp estiver conectado, use-o
   para confirmar título/link. Se não houver referência nenhuma, escreva "não aplicável" — não
   pergunte nem invente.
2. **Data** — data atual do sistema.
3. **Responsáveis** — rode `git config user.name` e `git config user.email` para o autor humano;
   sempre inclua Claude Code como par.
4. **Escopo e decisões** — baseie-se no diff (`git diff` vs a branch alvo) e no que foi discutido
   na conversa, não invente contexto que não apareceu no trabalho.

Nunca sobrescreva um arquivo de memória existente sem avisar — se já existir um arquivo para a
mesma data/slug, pergunte se é para atualizar ou criar um novo com sufixo.
