# Desafio Técnico – Kanban Backend (JWT, SOLID, TDD)


URL e Usuário e Senha para acessa o sistema Funcionando Publicado

http://app1.cdxsistemas.com.br:8080
"email": "teste@teste.com.br", 
"password": "admin"

http://app1.cdxsistemas.com.br:8080/swagger-ui.html (Aberto para avaliação)

## Banco de Dados (externo)
- **Host**: app1.cdxsistemas.com.br:5432
- **Banco**: codex | **Usuário**: codex | **Senha**: Ric@7901
- `spring.jpa.hibernate.ddl-auto=update` → **tabelas geradas automaticamente**.

## Rodando
```bash
mvn spring-boot:run
# ou Docker
docker compose up -d --build
```
Swagger: http://localhost:8080/swagger-ui.html

Publicado Servidor - Esta No ar

http://app1.cdxsistemas.com.br:8080/swagger-ui.html

## Autenticação JWT
- `POST /api/v1/auth/login`
```json
{ "email": "teste@teste.com.br", "password": "admin" }


"Repositório" Remoto 

Backend https://github.com/ricardomasterdev/backend-kanban
Frontend https://github.com/ricardomasterdev/frontend-Kanba



```
Use `Authorization: Bearer <token>` nas demais rotas.

## Escopo atendido (conforme teste)
- CRUD de **Projeto** e **Responsável**; **Secretaria** (diferencial).
- **Kanban**: listar por status e **transições** com validações.
- Cálculos: **diasAtraso** e **percentualTempoRestante**.
- **Swagger/OpenAPI**, **Docker**, **tests** (TDD base), **SOLID/Clean** nas camadas.
- **Seed** cria `admin@codex.local` / senha `admin` com role `ADMIN`.

## Estrutura
- `domain/`, `repository/`, `service/` (regras e transições), `web/` (controllers), `security/` (JWT).
- MapStruct para DTOs, Exception handler padronizado, paginação, auditoria.

## Próximos passos (sugestões)
- Testcontainers de integração, mais cenários de transição e erros (A→C, C→A, etc.).
- Indicadores (opcional): quantidade por status