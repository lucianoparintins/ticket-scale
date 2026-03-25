# Changelog

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Módulo de Autenticação com JWT:
  - Entidade `Usuario` e enum `Papel`.
  - `AutenticacaoController` para login e `UsuarioController` para cadastro.
  - `TokenService` para geração e validação de tokens JWT usando JJWT 0.12.6.
  - `SecurityFilter` e `SecurityConfigurations` para proteção stateless de endpoints.
- Suíte de Testes Automatizados:
  - Testes unitários para `TokenService` e `AutenticacaoService`.
  - Testes de controller com `MockMvc` para `AutenticacaoController`.
  - Testes de integração de segurança (`SegurancaTest`) com banco H2 em memória.
- Configuração de ambiente de teste:
  - Dependência do banco H2.
  - `src/test/resources/application.yml` configurado para testes isolados.
- Documentação:
  - Orientações sobre Java 25 e estratégia de testes no `GEMINI.md`.
  - Roadmap atualizado no `README.md`.

### Changed
- `build.gradle`: Atualizado para incluir starters de teste modulares do Spring Boot 4.0.4 e dependências de segurança/JJWT.

## [0.0.1] - 2026-03-24

### Added
- Scaffolding inicial do projeto Spring Boot 4.0.4 com Java 25.
- Estrutura de pacotes seguindo Clean Architecture.
- Configuração de infraestrutura com Docker Compose (PostgreSQL, Redis, RabbitMQ).
- Documentação inicial (`README.md` e `GEMINI.md`).
