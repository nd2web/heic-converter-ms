---
description: Prova que a feature funciona de ponta a ponta (UI via MCP do Playwright, API via curl) — não só que compila.
argument-hint: [caminho-da-spec]
---
Leia a spec em `$ARGUMENTS` e sua seção de critérios de aceite. Para cada critério:

1. Se a spec descreve uma interface web (telas, componentes, cliques), use as tools do MCP do
   Playwright (`@playwright/mcp`) para abrir a aplicação, navegar e interagir como o critério
   descreve — a checagem é pela árvore de acessibilidade da página, não por screenshot. Se o MCP
   não estiver conectado (`/mcp` não lista `playwright`), avise e pare — não simule o resultado.
2. Se a spec descreve uma API (endpoint, método, corpo, status), monte o `curl` equivalente aos
   "Requisitos de saída" da spec e rode contra o serviço já no ar. Se o serviço não estiver no ar,
   suba-o primeiro (ex. `npm run dev`).
3. Nunca marque um critério como atendido sem rodar a checagem de verdade — "parece certo" não é
   evidência.

Ao final, produza uma tabela `critério → passou? → evidência (comando/screenshot)`. Não corrija
nada — se algo falhar, reporte e pare; a correção é outra etapa (ver skill `bug-research`).
