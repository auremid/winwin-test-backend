# WinWin.travel backend assignment

A small two-service Spring Boot backend. `auth-api` registers users, authenticates them with JWTs, calls `data-api` for uppercase transformation, and records successful processing in PostgreSQL.

## Architecture

```text
client
  |-- POST /api/auth/* --------> auth-api :8080 ----> PostgreSQL
  `-- POST /api/process (JWT) --> auth-api :8080
                                      |
                                      `-- POST /api/transform
                                          X-Internal-Token
                                          data-api :8081
```

- `auth-api`: Spring Web, Spring Security, OAuth2 Resource Server JWT, Spring Data JPA, Flyway, BCrypt.
- `data-api`: Spring Web and Bean Validation.
- `postgres`: PostgreSQL 17 with a persistent Docker volume.
- Java 21, Maven, Spring Boot 3.5.16, Docker Compose.

## Prerequisites

- Docker Engine with Docker Compose, or
- Java 21 and Maven 3.6.3+ for local builds.

## Configure and start

Create local configuration and replace every placeholder:

```bash
cp .env.example .env
openssl rand -base64 32  # use as JWT_SECRET
openssl rand -hex 32     # use as INTERNAL_TOKEN
openssl rand -base64 24  # can be used as POSTGRES_PASSWORD
```

`JWT_SECRET` must be valid Base64 that decodes to at least 32 bytes. Secrets and `.env` are ignored by Git.
The optional `AUTH_API_PORT` and `DATA_API_PORT` settings can change host ports if 8080 or 8081 is already occupied; service-to-service ports do not change.

Build and start all three containers:

```bash
docker compose up -d --build
docker compose ps
docker compose logs --no-log-prefix auth-api data-api postgres
```

Flyway creates the database schema automatically when `auth-api` starts. The application waits for PostgreSQL's health check before starting.

## Use the API

Register:

```bash
curl --fail-with-body -i \
  -H 'Content-Type: application/json' \
  -d '{"email":"a@a.com","password":"pass"}' \
  http://localhost:8080/api/auth/register
```

Login and capture the token (requires `jq`):

```bash
TOKEN="$(curl --fail-with-body -sS \
  -H 'Content-Type: application/json' \
  -d '{"email":"a@a.com","password":"pass"}' \
  http://localhost:8080/api/auth/login | jq -r '.token')"
```

Process text:

```bash
curl --fail-with-body -sS \
  -H "Authorization: Bearer ${TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"text":"hello"}' \
  http://localhost:8080/api/process
```

The expected response is `{"result":"HELLO"}`. Calling `/api/process` without a valid bearer token returns `401`. Calling `data-api` without its internal header returns `403`:

```bash
curl -i -H 'Content-Type: application/json' \
  -d '{"text":"hello"}' \
  http://localhost:8081/api/transform
```

## Inspect processing logs

```bash
docker compose exec postgres psql \
  -U "${POSTGRES_USER:-winwin}" \
  -d "${POSTGRES_DB:-winwin}" \
  -c 'SELECT id, user_id, input_text, output_text, created_at FROM processing_log ORDER BY created_at;'
```

Passwords are stored only as BCrypt hashes in `users.password_hash`.

## Build and test

Run everything from the project root:

```bash
mvn clean test
mvn clean package
```

Or run each service independently:

```bash
mvn -f auth-api/pom.xml test
mvn -f data-api/pom.xml test
```

The auth integration tests use an in-memory H2 database and mock only the outbound data-service boundary. The data-service tests exercise its HTTP endpoint with valid, invalid, and missing internal tokens.

For a local non-Docker `auth-api` run, export `POSTGRES_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `JWT_SECRET`, `INTERNAL_TOKEN`, and optionally `DATA_API_URL`. `data-api` requires `INTERNAL_TOKEN`.

## Stop or clean up

Stop containers while keeping database data:

```bash
docker compose down
```

Stop containers and permanently remove the PostgreSQL volume:

```bash
docker compose down --volumes
```
