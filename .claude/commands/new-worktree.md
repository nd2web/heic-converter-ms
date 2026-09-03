---
description: Cria um git worktree isolado para trabalhar numa branch em paralelo.
argument-hint: [nome-da-branch]
---
Crie um git worktree para a branch `$ARGUMENTS` em um diretório irmão, sem alterar o working
directory atual: `git worktree add ../$ARGUMENTS-wt -b $ARGUMENTS`. Explique como alternar e como
remover (`git worktree remove`) ao terminar.
