# PWorks Login and Session Management

Reactive Spring Boot service that signs users in through Google, Microsoft, or Apple
and manages a short-lived session that shows the signed-in user's email and full name.

## Architecture

```text
User (browser)
     │
     ▼
PWorks app  ── Docker container, Spring WebFlux
     │
     ▼
Security module ── Spring Security OAuth 2.0 / OpenID Connect client
     │
     ▼
Identity providers: Google · Microsoft · Apple
```

Sign-in flow:

1. The user opens the app and picks an identity provider.
2. The security module redirects to that provider.
3. The provider returns the user's email and full name.
4. The app stores them in a session and shows the home page.

## Features

- Sign in with Google, Microsoft, or Apple
- Home page displays the signed-in user's email and full name
- Sign out button closes the session
- Session expires after 5 minutes
- In-memory session store, single instance

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Java v25, latest LTS release |
| Framework | Spring Boot v4.1.2, latest stable release |
| Web | Spring WebFlux (reactive) |
| Security | Spring Security OAuth 2.0 Client / OpenID Connect |
| Build | Maven (`mvnw` wrapper) |
| Session | In-memory reactive `WebSession` |
| Containers | Docker, Docker Compose |
| Testing | JUnit 5, Mockito |
| Persistence | None yet — MongoDB planned |

Versions are pinned in `pom.xml` once the project skeleton exists.

## Prerequisites

- JDK matching the version in `pom.xml` (latest LTS)
- Docker with Docker Compose
- An OAuth client registered with each provider you want to enable, with the
  redirect URI `http://localhost:8080/login/oauth2/code/{provider}`

## Configuration

All credentials and client IDs come from environment variables. Nothing secret is
committed to the repository.

| Variable | Description |
| --- | --- |
| `GOOGLE_CLIENT_ID` | Google OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth client secret |
| `MICROSOFT_CLIENT_ID` | Microsoft Entra ID application (client) ID |
| `MICROSOFT_CLIENT_SECRET` | Microsoft client secret |
| `APPLE_CLIENT_ID` | Apple service ID |
| `APPLE_TEAM_ID` | Apple developer team ID |
| `APPLE_KEY_ID` | Apple private key ID |
| `APPLE_PRIVATE_KEY` | Apple private key (PEM) |

> Never commit client secrets, keys, or tokens. Use environment variables or a
> git-ignored local file.

## Getting Started

### Run with Docker Compose

```bash
docker compose up --build
```

The app is available at <http://localhost:8080>.

### Run locally

```bash
./mvnw spring-boot:run
```

### Try it

1. Open <http://localhost:8080>.
2. Choose Google, Microsoft, or Apple.
3. Complete the provider's sign-in flow.
4. The home page shows your email and full name.
5. Use the sign-out button to close the session.

## Session Behavior

- Created on successful sign-in and holds the user's email and full name.
- Expires after 5 minutes, set by `server.reactive.session.timeout=5m` in
  `application.yml`.
- Stored in memory only: sessions are lost on restart and are not shared across
  instances.
- Extending a session from a frontend dialog is planned; no endpoint exists yet.

## Testing

```bash
./mvnw test
```

Unit tests use JUnit 5 and Mockito. Integration tests with Testcontainers come later,
together with MongoDB and Redis.

## Project Structure

Planned Maven layout:

```text
.
├── docker-compose.yml
├── pom.xml
├── README.md
├── LOG.md
└── src/
    ├── main/java/          # application and security configuration
    ├── main/resources/     # application.yml
    └── test/java/          # unit tests
```

## Roadmap

- MongoDB for user persistence
- Redis for session cache and multi-instance sessions
- Frontend dialog to extend an active session
- Testcontainers integration tests
- Docker Compose services for Redis, MongoDB, and other dependencies
- Public GitHub repository with CI

## Contributing

The project is currently a local repository only. Branch, commit, and review
conventions will be documented before the first public push.

## License

To be decided before the repository is made public.
