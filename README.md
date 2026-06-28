# Bookshop Service

A Spring Boot 3 REST API for a bookshop — featuring JWT authentication, OAuth 2.0 Authorization Code Flow, personalized book listings, pagination, and HATEOAS.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.5 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Build | Gradle (Kotlin DSL) |
| Utilities | Lombok |

## Architecture

The project follows a **Domain-Driven Design (DDD)** / Clean Architecture layout, organized into bounded contexts:

```
com.psb.bookshop
├── catalog/          # Book catalogue (list all books)
│   ├── application/  # Use cases & DTOs
│   ├── domain/       # Domain model & repository interfaces
│   ├── infrastructure/ # In-memory repository impl
│   └── interfaces/   # REST controllers
├── identity/         # User registration & direct login
├── me/               # Personalized named-user endpoints
├── oauth/            # OAuth 2.0 Authorization Code Flow
└── shared/           # Cross-cutting: JWT, security config
```

## API Overview

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/auth/register` | Public | Register a new user |
| `POST` | `/auth/login` | Public | Login and receive a JWT |
| `GET` | `/books` | Public | List all books |
| `GET` | `/oauth/authorize` | Public | Start OAuth 2.0 flow |
| `POST` | `/oauth/token` | Public | Exchange auth code for token |
| `GET` | `/me/profile` | 🔒 Bearer | Get authenticated user's profile |
| `GET` | `/me/books` | 🔒 Bearer | Get personalized paginated book list |

Full API spec: [`docs/openapi.yaml`](docs/openapi.yaml)

## Authentication

### Direct Login (first-party clients)

```bash
# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "password": "secret1234"}'

# Login → returns JWT
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "password": "secret1234"}'

# Call a protected endpoint
curl http://localhost:8080/me/profile \
  -H "Authorization: Bearer <access_token>"
```

### OAuth 2.0 Authorization Code Flow (third-party apps)

1. Redirect user to `GET /oauth/authorize?response_type=code&client_id=...&redirect_uri=...&scope=books:read profile:read&state=...`
2. User logs in and approves scopes
3. Auth server redirects to `redirect_uri?code=AUTH_CODE`
4. Exchange code: `POST /oauth/token` with `grant_type=authorization_code`
5. Use the returned JWT as `Authorization: Bearer <token>`

#### OAuth Scopes

| Scope | Access |
|-------|--------|
| `books:read` | Read the user's personalized book list |
| `profile:read` | Read the user's profile |

## Running Locally

**Prerequisites:** Java 21, Gradle (or use the wrapper)

```bash
# Clone the repository
git clone https://github.com/psparmeet14/bookshop-service.git
cd bookshop-service

# Run the application
./gradlew bootRun
```

The service starts at `http://localhost:8080`.

## Running Tests

```bash
./gradlew test
```

## Configuration

Key properties in `src/main/resources/application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `jwt.secret` | `bookshop-super-secret-key-...` | HMAC signing secret (override in production) |
| `jwt.expiry-ms` | `3600000` | Token expiry in milliseconds (1 hour) |

> ⚠️ **Note:** The default JWT secret is for local development only. Always override it with a strong secret in production environments.
