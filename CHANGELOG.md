# Changelog

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
## [Unreleased]

### Added
- Documentação de análise inicial do projeto e roadmap estratégico em `docs/analise_projeto_inicial.md`.
- Configuração de perfis de aplicação:
  - `application.yml`: Configurações comuns e ativação do perfil `dev` por padrão.
  - `application-dev.yml`: Configurações locais (PostgreSQL, Redis, RabbitMQ em localhost).
  - `application-prod.yml`: Configurações via variáveis de ambiente para produção.
- Workflow de commit obrigatório:
  - Exigência de atualização de `CHANGELOG.md`, `README.md` e `GEMINI.md` antes de cada commit.

### Changed
- `application.yml` original: Decomposto nos perfis `dev` e `prod` para melhor gestão de ambientes.

## [0.0.1] - 2026-03-24

### Added
- Módulo de Autenticação com JWT:
...
- Estrutura de pacotes seguindo Clean Architecture.
- Configuração de infraestrutura com Docker Compose (PostgreSQL, Redis, RabbitMQ).
- Documentação inicial (`README.md` e `GEMINI.md`).
