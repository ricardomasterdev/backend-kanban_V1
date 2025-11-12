# Desafio Técnico – Kanban (Backend + Frontend)

> **Status**: ✅ Publicado em homologação (HML) e pronto para rodar localmente  
> **IDE usada**: IntelliJ IDEA (2025) — Backend **e** Frontend  
> **JDK**: **Java 21** | **Spring Boot** 3.3.x | **PostgreSQL** 15+ | **JWT** | **SOLID/TDD**

---

## 🔗 URLs públicas (HML)

- **Backend (Swagger/OpenAPI)**: http://app1.cdxsistemas.com.br:8080/swagger-ui.html  
- **Login (JWT)**: `POST /api/v1/auth/login`  
  ```json
  { "email": "teste@teste.com.br", "password": "admin" }
  ```
  Use o header `Authorization: Bearer <token>` nas demais rotas.

---

## 🧱 Banco de Dados (externo HML)

- **Host**: `app1.cdxsistemas.com.br:5432`
- **Banco**: `codex`
- **Usuário**: `codex`
- **Senha**: `Ric@7901`
- Propriedade recomendada: `spring.jpa.hibernate.ddl-auto=update` (gera/atualiza tabelas automaticamente).

> ⚠️ **Credenciais estão públicas apenas para avaliação.** Em produção, use variáveis de ambiente/secret manager.

---

## 🧰 Tecnologias (Backend)

- **Java 21**, **Spring Boot 3.3.x**
- Spring Web, Validation, Data JPA, Security, Actuator
- PostgreSQL Driver
- **JWT** (`io.jsonwebtoken`)
- **MapStruct** (DTOs/Mapper)
- **OpenAPI/Swagger** (`springdoc-openapi`)
- **TDD** com JUnit + Testcontainers (sugestão pronta no `pom.xml`)
- **SOLID/Clean**: camadas `domain/`, `repository/`, `service/` (regras de negócio), `web/` (controllers), `security/` (JWT)

### `pom.xml` (trecho relevante)
- Configurado para **Java 21**, Spring Boot 3.3.x, MapStruct, JJWT.
- **Compilação** com `maven-compiler-plugin` e annotation processors do MapStruct.
- **Jacoco** para relatório de cobertura.

---

## ▶️ Rodando o Backend

### IntelliJ IDEA (2025)
1. Configure o **Project SDK** como **JDK 21**.
2. Importe o projeto Maven (auto-import ON).
3. Perfil padrão: `default` (ou `dev`).  
4. **Run** → `KanbanApplication` (ou classe principal do Spring Boot).

### Maven (local)
```bash
mvn spring-boot:run
```
> Por padrão, ele lerá `application.properties`/`application.yml`. Ajuste host/credenciais do PostgreSQL conforme seu ambiente.

### Docker
```bash
docker compose up -d --build
```
> O `compose` pode expor a API em `:8080` e apontar para o PostgreSQL externo, ou para um container Postgres local. Ajuste os `environment` conforme necessidade.

---

## ⚙️ Configuração do Backend

### `application.yml` (exemplo)
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://app1.cdxsistemas.com.br:5432/codex
    username: codex
    password: Ric@7901
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
  jackson:
    serialization:
      WRITE_DATES_AS_TIMESTAMPS: false

# CORS para liberar o frontend em HML
cors:
  allowed-origins: "http://localhost:5173,http://127.0.0.1:5173,https://seu-dominio-de-front-hml"
  allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
  allowed-headers: "*"
  allow-credentials: true

jwt:
  secret: "troque-este-segredo-em-producao"
  expiration: 86400000 # 1 dia em ms
```

### CORS (config Java – exemplo rápido)
Caso deseje forçar via código:
```java
@Bean
public WebMvcConfigurer corsConfigurer(@Value("${cors.allowed-origins:*}") String origins,
                                       @Value("${cors.allowed-methods:*}") String methods,
                                       @Value("${cors.allowed-headers:*}") String headers) {
  return new WebMvcConfigurer() {
    @Override public void addCorsMappings(CorsRegistry registry) {
      registry.addMapping("/**")
        .allowedOrigins(origins.split(","))
        .allowedMethods(methods.split(","))
        .allowedHeaders(headers.split(","))
        .allowCredentials(true);
    }
  };
}
```

---

## 🧮 Regras de Negócio – Kanban

- **Status**: `A_INICIAR`, `EM_ANDAMENTO`, `ATRASADO`, `CONCLUIDO`
- **Transições com validações** (exemplos):
  - `A_INICIAR → EM_ANDAMENTO`: seta `inicioRealizado = hoje`.
  - `EM_ANDAMENTO → CONCLUIDO`: seta `terminoRealizado = hoje`.
  - `CONCLUIDO → EM_ANDAMENTO`: limpa `terminoRealizado`; se virar `ATRASADO`, bloquear/sugerir ajuste de previstas.
  - `* → ATRASADO`: somente se `terminoPrevisto < hoje` (e sem `terminoRealizado`).
- **Cálculos** (persistidos no BD via trigger):
  - `dias_atraso`: `GREATEST(0, CURRENT_DATE - termino_previsto)` quando **não concluído**.
  - `percentual_tempo_restante`: baseado no intervalo `[inicio_previsto..termino_previsto]` e data atual. Se fora da janela, 0%/100%.

---

## 🗃️ Modelo `projeto` (DDL) + Procedure/Trigger (PostgreSQL)

> Abaixo, um **.sql** completo para: tabela, função de cálculo e triggers de `INSERT/UPDATE`.  
> Aplique no **mesmo schema** onde a aplicação lê/escreve (por padrão `public`).

```sql
-- Tabela (ajuste se já existir)
CREATE TABLE IF NOT EXISTS public.projeto (
  id                     UUID PRIMARY KEY,
  created_at             timestamptz NOT NULL DEFAULT now(),
  updated_at             timestamptz NOT NULL DEFAULT now(),
  nome                   varchar(120) NOT NULL,
  status                 varchar(20)  NOT NULL,
  secretaria_id          bigint NULL,

  inicio_previsto        date NULL,
  termino_previsto       date NULL,
  inicio_realizado       date NULL,
  termino_realizado      date NULL,

  -- campos calculados
  dias_atraso                    int  NOT NULL DEFAULT 0,
  percentual_tempo_restante      int  NOT NULL DEFAULT 0,

  CONSTRAINT projeto_status_check CHECK (status IN ('A_INICIAR','EM_ANDAMENTO','ATRASADO','CONCLUIDO'))
);

CREATE INDEX IF NOT EXISTS idx_projeto_status         ON public.projeto(status);
CREATE INDEX IF NOT EXISTS idx_projeto_secretaria_id  ON public.projeto(secretaria_id);

-- Função: calcula dias_atraso e percentual_tempo_restante
CREATE OR REPLACE FUNCTION public.fn_calc_projeto_metrics(
  p_inicio_previsto date,
  p_termino_previsto date,
  p_inicio_realizado date,
  p_termino_realizado date
) RETURNS TABLE (dias_atraso int, percentual int) LANGUAGE plpgsql AS
$$
DECLARE
  v_today date := CURRENT_DATE;
  v_dias int := 0;
  v_pct int := 0;
  v_total int;
  v_passou int;
BEGIN
  -- dias_atraso: somente se não concluído e há término previsto
  IF p_termino_realizado IS NULL AND p_termino_previsto IS NOT NULL THEN
    v_dias := GREATEST(0, (v_today - p_termino_previsto));
  END IF;

  -- percentual_tempo_restante
  IF p_inicio_previsto IS NOT NULL AND p_termino_previsto IS NOT NULL THEN
    v_total := GREATEST(1, (p_termino_previsto - p_inicio_previsto)); -- evita div/0
    v_passou := GREATEST(0, LEAST(v_total, (v_today - p_inicio_previsto)));
    v_pct := ROUND(100 - (v_passou::numeric * 100.0 / v_total));
    v_pct := GREATEST(0, LEAST(100, v_pct));
  ELSE
    v_pct := 0;
  END IF;

  RETURN QUERY SELECT v_dias, v_pct;
END;
$$;

-- Trigger function: preenche campos calculados + update timestamp
CREATE OR REPLACE FUNCTION public.trg_projeto_metrics_bi_bu()
RETURNS trigger LANGUAGE plpgsql AS
$$
DECLARE
  r record;
BEGIN
  SELECT * INTO r
  FROM public.fn_calc_projeto_metrics(NEW.inicio_previsto, NEW.termino_previsto, NEW.inicio_realizado, NEW.termino_realizado);

  NEW.dias_atraso := COALESCE(r.dias_atraso, 0);
  NEW.percentual_tempo_restante := COALESCE(r.percentual, 0);
  NEW.updated_at := now();

  RETURN NEW;
END;
$$;

-- Triggers BEFORE INSERT/UPDATE
DROP TRIGGER IF EXISTS trg_projeto_metrics_bi ON public.projeto;
CREATE TRIGGER trg_projeto_metrics_bi
BEFORE INSERT ON public.projeto
FOR EACH ROW EXECUTE FUNCTION public.trg_projeto_metrics_bi_bu();

DROP TRIGGER IF EXISTS trg_projeto_metrics_bu ON public.projeto;
CREATE TRIGGER trg_projeto_metrics_bu
BEFORE UPDATE ON public.projeto
FOR EACH ROW EXECUTE FUNCTION public.trg_projeto_metrics_bi_bu();
```

> **Dica**: Se você já tem a tabela, apenas execute a **function** e os **triggers**. O `UPDATE` a partir daí recalculará os campos. Para recalcular já existentes, rode um `UPDATE public.projeto SET nome = nome;` (força trigger).

---

## 🌐 Endpoints principais (resumo)

- `POST /api/v1/auth/login` → retorna JWT
- `GET /api/v1/kanban` → board por status
- `POST /api/v1/kanban/{id}/transicao` → muda status (aceita `para`, `status` ou `novoStatus` no body, para máxima compatibilidade)
- CRUD:
  - `GET/POST /api/v1/projetos`
  - `GET/PUT/DELETE /api/v1/projetos/{id}`
  - `POST/DELETE /api/v1/projetos/{id}/responsaveis/{rid}` (se disponível)
- `GET /api/v1/responsaveis`, `GET /api/v1/secretarias` (filtros de autocomplete)

---

## 💻 Frontend (React + Vite + TS)

### Scripts (package.json)
```json
{
  "scripts": {
    "dev": "vite",
    "dev:hml": "vite --mode hml",
    "dev:prod": "vite --mode prod",
    "build": "tsc -b && vite build",
    "build:hml": "tsc -b && vite build --mode hml",
    "build:prod": "tsc -b && vite build --mode prod",
    "preview": "vite preview",
    "preview:hml": "vite preview",
    "preview:prod": "vite preview"
  }
}
```

### `.env` por ambiente
Crie na raiz do **frontend**:
```
# .env.development (localhost)
VITE_API_BASE=http://localhost:8080

# .env.hml (homologação)
VITE_API_BASE=http://app1.cdxsistemas.com.br:8080

# .env.production (produção)
VITE_API_BASE=https://SEU_DOMINIO_PRODUCAO
```

### `vite.config.ts` – liberar host do preview (erro “Blocked request…”)
```ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => ({
  plugins: [react()],
  server: {
    host: true,
    port: 5173,
    proxy: {
      // opcional: roteia chamadas /api para backend local
      // '/api': 'http://localhost:8080'
    }
  },
  preview: {
    allowedHosts: ['app1.cdxsistemas.com.br'] // ✅ corrige o erro de host bloqueado
  }
}))
```

### `src/services/api.ts` – baseURL por modo
```ts
import axios from 'axios'
// Tipos do Vite para import.meta.env
/// <reference types="vite/client" />

const base =
  (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.VITE_API_BASE)
    ? import.meta.env.VITE_API_BASE
    : 'http://localhost:8080'

export const api = axios.create({
  baseURL: base,
  withCredentials: false
})

// Interceptor para JWT (se você salvou o token no localStorage)
api.interceptors.request.use(cfg => {
  const token = localStorage.getItem('authToken')
  if (token) cfg.headers['Authorization'] = `Bearer ${token}`
  return cfg
})
```

### Rodando o frontend
```bash
# desenvolvimento local
npm i
npm run dev

# homologação (aponta para o backend HML)
npm run dev:hml           # (ou) build/hospedar: npm run build:hml && npm run preview
```

> Caso a página “abra direto no Kanban” em HML, é porque **o token já está salvo** no `localStorage`. Limpe o armazenamento do navegador para testar o fluxo de login de novo.

---

## 🔐 Autenticação JWT (fluxo)
1. `POST /api/v1/auth/login` com email/senha.
2. Salve `accessToken` no `localStorage` (ex.: `authToken`).
3. Envie `Authorization: Bearer <token>` em todas as requisições (interceptor Axios).
4. Em `401`, redirecione para login e limpe token.

---

## 🧪 TDD e Qualidade
- Testes unitários com `spring-boot-starter-test`.
- (Opcional) **Testcontainers** para integração com Postgres em memória.
- **Jacoco** configurado no `pom.xml` (gera relatório em `target/site/jacoco`).

---

## 📦 Repositórios
- **Backend**: https://github.com/ricardomasterdev/backend-kanban_V1
- **Frontend**: https://github.com/ricardomasterdev/frontend-Kanba_V1

---

## 🛠️ Troubleshooting

- **401 Unauthorized no HML**  
  Faça login e confira se o token está no `localStorage`. Se o backend exigir CORS, garanta os domínios liberados (ver seção CORS).

- **“Blocked request. This host (...) is not allowed.” no Vite Preview**  
  Confirme `preview.allowedHosts` no `vite.config.ts` (seção frontend).

- **Build TypeScript erro com `import.meta.env`**  
  Inclua `/// <reference types="vite/client" />` no topo do arquivo `api.ts` ou adicione `"types": ["vite/client"]` no `tsconfig.json`.

- **Datas e atraso não calculam**  
  Verifique se a **function/trigger** do Postgres foi criada (seção SQL). Forçar recálculo: `UPDATE public.projeto SET nome = nome;`.

---

## ✅ Escopo Atendido
- CRUD de **Projeto** e **Responsável**; **Secretaria** (diferencial).
- **Kanban** com transições e validações.
- Cálculos de **dias_atraso** e **percentual_tempo_restante** no **BD** (trigger).
- **Swagger/OpenAPI**, **Docker**, **TDD base**, **MapStruct**, **JWT**.
- Seed de usuário admin (conforme a versão disponibilizada).

---

## 🔒 Observações de Segurança
- Não commitar segredos. Use **variáveis de ambiente** ou secret manager para produção.
- Rotacione chaves JWT e credenciais de banco fora do contexto de avaliação.

---

**Pronto!** Com isso você consegue:
- Rodar **backend** (Java 21) na IntelliJ/Maven/Docker.
- Rodar o **frontend** apontando para **HML** (e corrigir o erro de host bloqueado).
- Persistir automaticamente os campos `dias_atraso` e `percentual_tempo_restante` via trigger no **PostgreSQL**.
