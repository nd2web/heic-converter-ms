# RULES — Padrões (Java / Spring / GitLab)

## Coding standards
- Java 21; nomes descritivos; classes coesas; sem código morto.
- Lombok permitido (@RequiredArgsConstructor, @Slf4j, @Getter/@Setter).
- Exceções de domínio estendem RuntimeException e carregam mensagem legível.
- Cobrir todos os critérios de aceite da spec com testes.

## Branch protection (GitLab)
- `main`/`master` protegidas: merge só via Merge Request aprovado + pipeline verde.
- Branches: `feature/<slug>`, `fix/<slug>`, `chore/<slug>`.
- 1 revisor humano além do ac-reviewer.

## Padrões de commit/PR
- Conventional Commits (`feat:`, `fix:`, `refactor:`...).
- MR descreve o quê, o porquê e como testar.

## Segurança / LGPD
- Serviço sem estado; não logar conteúdo de imagens nem PII.
- Timeout e limites de upload sempre configurados.
