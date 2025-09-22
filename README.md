# Customer Service App

This project is a Kotlin + Spring Boot 3.5 app using HTMX + Thymeleaf for UI, Postgres + Flyway for the database, and a scheduled importer that pulls products from famme.no. Web Awesome is used for styling/components.

This README gives you everything needed to run a local Postgres with Docker Compose, run the app, and test the UI and endpoints.

## Prerequisites
- Java 21 (project uses Java toolchain 21)
- Docker + Docker Compose
- IntelliJ IDEA Ultimate recommended (DB client + HTTP client)

## Start Postgres with Docker Compose
From the project root:

```bash
docker compose up -d
# Wait until status is healthy
docker compose ps
```

The DB will be available at:
- URL: jdbc:postgresql://localhost:5432/customer_service_app
- User: postgres
- Password: postgres

The application already has defaults matching this configuration, so no environment variables are required. If you wish, you can export them explicitly:

```bash
export JDBC_URL=jdbc:postgresql://localhost:5432/customer_service_app
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

## Run the application

```bash
./gradlew bootRun
```

On startup, Flyway will create the schema and a scheduled job will attempt to import up to 50 products from https://famme.no/products.json. To ensure the UI always has something to show, a tiny DataSeeder inserts a sample product if the table is empty.

Open the UI in your browser:
- http://localhost:8080/
- Click the "Load products" button to load the products table via HTMX without a full page refresh.

## Hot reloading (no manual restarts)
This project ships with Spring Boot DevTools for fast reloads during development.

What you get:
- Automatic restart of the application when Kotlin/Java classes or resources on the classpath change
- A built-in LiveReload server that can refresh the browser when templates/static files change

How to use:
1. Run the app in development mode (the default):
   ```bash
   ./gradlew bootRun
   ```
2. In IntelliJ IDEA, enable automatic compilation so changes are picked up immediately:
   - macOS: Preferences → Build, Execution, Deployment → Compiler → check "Build project automatically"
   - Then go to Preferences → Advanced Settings → check "Allow auto-make to start even if developed application is currently running"
3. Optional: Install a LiveReload browser extension and enable it on http://localhost:8080 so the page refreshes automatically when you change templates or static assets. DevTools exposes the LiveReload server on port 35729.

Notes:
- Thymeleaf caching is disabled (spring.thymeleaf.cache=false), so template changes are reflected immediately.
- Code changes trigger a quick application restart (not a full JVM reload). Long-lived state should be avoided in dev.
- DevTools is included as a developmentOnly dependency; it is not packaged into production builds.

## Test using IntelliJ HTTP Client
There’s a prebuilt HTTP requests file at `http/requests.http`. In IntelliJ, open this file and run:

```
GET http://localhost:8080/
GET http://localhost:8080/products
```

## Inspect the database in IntelliJ
- Open the Database tool window, add a PostgreSQL data source:
  - Host: localhost
  - Port: 5432
  - Database: customer_service_app
  - User: postgres
  - Password: postgres
- Run queries like:

```sql
select count(*) from products;
select id, title, price, url from products order by id desc limit 10;
```

## Run integration test (optional)
A Testcontainers integration test is included, gated behind an env var to avoid requiring Docker on every test run:

```bash
export ENABLE_DOCKER_TESTS=true
./gradlew test --tests "*ProductRepositoryIT*"
```

## Troubleshooting
- Port conflict on 5432: Change the compose mapping to `15432:5432` and set `JDBC_URL=jdbc:postgresql://localhost:15432/customer_service_app`.
- Importer errors: The app logs failures but continues to run; try reloading later or check logs. You can still view the UI and the seeded sample product.
- Reset DB: `docker compose down -v && docker compose up -d`

## Tech summary
- Spring Boot 3.5.6, Kotlin 2.0.21, Java 21 toolchain
- Flyway migrations under `src/main/resources/db/migration`
- Postgres 16 (via Docker Compose)
- HTMX + Thymeleaf for server-rendered UI
- Web Awesome for components + design tokens

## Why do we import /css/app.css if Web Awesome docs don’t mention it?
Web Awesome provides the design system (tokens, themes, components). Our `app.css` is a tiny, site-specific layer where we:
- Apply layout primitives (container width, card spacing) using Web Awesome tokens
- Polish the table (sticky header, zebra striping, hover) and form spacing
- Add small UX niceties (skeleton placeholder, button loading spinner)

It’s optional. If you want to rely only on Web Awesome defaults, delete the line in `templates/index.html`:

```html
<link rel="stylesheet" href="/css/app.css?v=2">
```

The UI will still work with Web Awesome’s default theme, but you’ll lose our custom spacing and table polish. You can also trim `app.css` to only keep what you like.
