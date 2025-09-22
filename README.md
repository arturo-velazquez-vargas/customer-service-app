# Customer Service App

A modern Kotlin + Spring Boot web app that showcases a marketplace-like UI using HTMX + Thymeleaf and Web Awesome components. Data is stored in Postgres with Flyway migrations. A scheduled job imports up to 50 products from famme.no and stores variants as JSONB.

This README covers setup, development workflow (with hot reload), key features, and troubleshooting.

## Features
- Products page with HTMX-powered table that loads and refreshes without full page reloads
- Add Product form (Web Awesome components) that inserts and updates the table inline
- Live search page with active results as you type
- Edit product page (update title, price, URL)
- Delete product with HTMX confirmation and inline refresh
- Scheduled importer (initialDelay=0) pulls products from https://famme.no/products.json (max 50)
- Variants persisted in a JSONB column for flexibility

## Tech stack (versions)
- Spring Boot 3.5.6
- Kotlin 2.0.21
- Java toolchain 25 (emits Java 22 bytecode for Kotlin compatibility)
- Postgres 16 + Flyway
- HTMX + Thymeleaf
- Web Awesome v3 beta (components + design tokens)
- Testcontainers (optional integration test)

## Quick start
1) Start Postgres with Docker Compose
```bash
docker compose up -d
# Wait until the container is healthy
docker compose ps
```
Database connection (defaults):
- JDBC URL: `jdbc:postgresql://localhost:5432/customer_service_app`
- User: `postgres`
- Password: `postgres`

2) Run the app
```bash
./gradlew bootRun
```
What happens on startup:
- Flyway applies migrations (creates `products` table with JSONB `variants`)
- Scheduled importer runs immediately (initialDelay=0) and tries to import up to 50 products
- If still empty, a small DataSeeder inserts a sample row to prove the UI works

3) Open the UI
- http://localhost:8080
- Click “Refresh” (or it may auto-load) to populate the products table

## Development: hot reload
Spring Boot DevTools is enabled to avoid manual restarts.

Enable in IntelliJ:
- Preferences → Build, Execution, Deployment → Compiler → check “Build project automatically”
- Preferences → Advanced Settings → check “Allow auto-make to start even if developed application is currently running”

Then run:
```bash
./gradlew bootRun
```
DevTools will:
- Restart the app on code/resource changes
- Provide a LiveReload server (port 35729). Install a LiveReload extension if you want auto page refresh on template/static changes.

Notes:
- Template cache is disabled: `spring.thymeleaf.cache=false`
- Virtual threads enabled: `spring.threads.virtual.enabled=true`
- Build config: Java toolchain 25; Kotlin/Java emit target 22 to avoid mismatch

## Environment variables
These are optional (defaults match docker-compose):
- `JDBC_URL` (default: `jdbc:postgresql://localhost:5432/customer_service_app`)
- `DB_USERNAME` (default: `postgres`)
- `DB_PASSWORD` (default: `postgres`)

## Project layout
- `src/main/resources/db/migration/V1__init.sql` — Flyway migration (products table + indexes)
- `src/main/kotlin/.../products/ProductRepository.kt` — JdbcClient queries (list, search, insert, update, delete, upsert by external_id)
- `src/main/kotlin/.../products/ProductImportScheduler.kt` — scheduled importer (limit 50, variants JSONB)
- `src/main/kotlin/.../web/HomeController.kt` — MVC endpoints
- `src/main/resources/templates/` — Thymeleaf templates (index, fragments/products-table, search, product-edit)
- `src/main/resources/static/css/app.css` — small site styling layer using Web Awesome tokens
- `docker-compose.yml` — Postgres 16 local DB
- `http/requests.http` — IntelliJ HTTP client examples

## Endpoints (human-facing)
- `GET /` — products page; HTMX loads table via `GET /products`
- `GET /products` — table fragment of latest products (limited)
- `POST /products/add` — add a product; returns updated table fragment
- `GET /search` — search page (live as you type)
- `GET /products/search?q=...` — table fragment filtered by title
- `GET /products/{id}/edit` — edit form
- `POST /products/{id}/edit` — update product then redirect
- `POST /products/{id}/delete` — delete product; returns updated table fragment

## Database schema (summary)
Table: `products`
- `id bigserial primary key`
- `external_id text unique` — used by importer for upsert
- `title text not null`
- `price numeric(12,2)`
- `url text`
- `variants jsonb` — raw variants array from upstream
- `created_at timestamptz default now()`, `updated_at timestamptz`
Indexes:
- GIN on `to_tsvector('simple', coalesce(title,''))` for search
- GIN on `variants`

## Search
Live, debounced search by title is available at `/search`. As you type, HTMX requests `/products/search?q=...` and swaps the table fragment.

## Testing
- Unit/integration: JUnit 5. Optional Postgres integration via Testcontainers.
- To run the gated integration test:
```bash
export ENABLE_DOCKER_TESTS=true
./gradlew test --tests "*ProductRepositoryIT*"
```
- HTTP requests: open `http/requests.http` in IntelliJ and run the requests.

## Troubleshooting
- Postgres port busy: change docker-compose mapping to `15432:5432` and set `JDBC_URL=jdbc:postgresql://localhost:15432/customer_service_app`.
- Importer errors: app continues to run; check logs and retry later. You can manually insert a product:
```sql
insert into products (title, price, url) values ('Local Test Product', 9.99, 'https://example.com');
```
- DevTools not reloading: ensure IntelliJ auto-make is enabled and code compiles on save.
- Template/CSS cache: we version the stylesheet link (`/css/app.css?v=3`); hard-refresh if styles seem stale.

## Why keep app.css in addition to Web Awesome?
Web Awesome provides components and tokens. `app.css` applies page layout (header, containers), table polish (sticky header, zebra rows), form spacing, and small UX details (skeletons, button spinners) using those tokens. You can trim or remove it if you prefer pure defaults.

---
Happy hacking! Open an issue if you spot something off or want to propose improvements.
