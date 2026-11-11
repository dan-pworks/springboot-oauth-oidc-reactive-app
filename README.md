# PWorks Login and Session Management

Reactive Spring Boot service that signs users in through Google, Microsoft, or Apple
and manages a short-lived session that shows the signed-in user's email, full name,
and tokens.

Maven artifact: `parra-works-web` · base package: `com.pworks.web.app.pww`

## Project Status

Microsoft sign-in is configured and works once credentials are supplied. The home
page also shows the OIDC ID token and the OAuth 2.0 access token. Google and Apple
are still pending.

| Area | State |
| --- | --- |
| Spring Boot 4.1.2 on Java 25, Maven | Done |
| WebFlux + Thymeleaf, devtools | Done |
| Docker Compose support (`spring-boot-docker-compose`) | Wired, no services defined |
| Login and home pages with controllers | Done |
| Spring Security, OAuth 2.0 client | Done |
| Microsoft OIDC registration | Done, needs client credentials |
| Google and Apple registrations | Not started |
| Public routes configurable in `application.yaml` | Done |
| ID token and access token on the home page | Done |
| Session with 5-minute expiry | Done, in memory |
| OIDC RP-initiated logout (local and Microsoft session) | Done |

## Recent Commits

Taken from `git log`; the repository history is authoritative.

| Commit | Branch | Summary |
| --- | --- | --- |
| `0dcb665` | `feature/add_oidc_oauth_support` | Wired Microsoft OIDC sign-in end to end; renamed login to `/authenticate`; added `SecurityConfig` and offline test configuration |
| `40a5418` | `feature/add_oidc_oauth_support` | Added `spring-boot-starter-security-oauth2-client` and security test utilities to `pom.xml` |
| `06f6632` | `main`, `feature/add_login_home_pages` | Added login and home pages, their controllers, `AGENTS.md`, and updated README and LOG |
| `4eec56e` | `feature/scaffold_project` | Added the Spring Boot project; updated README and LOG |
| `ee40e21` | initial | Added README and `.gitignore` |

## Architecture

```text
User (browser)
     │
     ▼
PWORKS App  ── Docker container, Spring WebFlux
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
3. The provider returns the user's email and full name plus the ID and access tokens.
4. The app stores the authorized client in the session and shows the home page.
5. Signing out clears the local session and ends the Microsoft session through
   OpenID Connect RP-initiated logout.

## Features

Available now:

- Sign in with Microsoft through OpenID Connect (authorization code flow)
- Sign-in page at `/authenticate` with **Sign in with Microsoft** and **Sign in
  with Google** buttons, each with its provider logo
- Home page at `/` with the user's name and email from the OIDC claims, plus the
  raw ID token and OAuth 2.0 access token, and a sign-out button
- Sign-out through OIDC RP-initiated logout: the local session is cleared and the
  user is sent to Microsoft's logout endpoint before returning to `/authenticate`
- Public routes listed in `application.yaml` under `app.security.endpoints.allowed`
  instead of being hardcoded in `SecurityConfig`
- Protected routes: anonymous or expired sessions are redirected to `/authenticate`
- Session expiry after 5 minutes, in-memory store

The tokens on the home page are for development and debugging. Remove that display
before exposing the app publicly.

Planned:

- Sign in with Google and Apple
- Session extension dialog on the frontend
- MongoDB persistence and Redis session cache

## Routes

| Route | Access | Purpose |
| --- | --- | --- |
| `GET /authenticate` | Public | Sign-in page with provider buttons |
| `GET /` | Authenticated | Home page with user info, ID token, access token, and sign-out |
| `GET /oauth2/authorization/microsoft` | Public | Starts the Microsoft login |
| `GET /login/oauth2/code/microsoft` | Public | Microsoft redirect URI |
| `POST /logout` | Public, CSRF-protected | Clears the session, ends the Microsoft session, returns to `/authenticate` |
| `GET /css/app.css` | Public | Shared stylesheet |

The provider buttons link to `/oauth2/authorization/{registrationId}` with the ids
`google` and `microsoft`. Only the `microsoft` registration exists, so the Google
button returns 404 until that registration is added; the ids must keep matching the
button links. The public routes themselves live in `application.yaml`
(`app.security.endpoints.allowed`) and are loaded into `SecurityConfig` by
`SecurityConfigProps`.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Java 25 |
| Framework | Spring Boot 4.1.2 |
| Web | Spring WebFlux, Reactor Netty |
| Views | Thymeleaf |
| Security | Spring Security WebFlux, OAuth 2.0 Client / OpenID Connect |
| Build | Maven, with `mvnw` wrapper scripts |
| Session | In-memory reactive `WebSession`, 5-minute timeout |
| Containers | Docker Compose via `spring-boot-docker-compose` |
| Testing | JUnit 5, AssertJ, Mockito, Spring Security test utilities |
| Persistence | None yet — MongoDB planned |

## Prerequisites

- JDK 25
- Maven, or a repaired Maven wrapper (see [Getting Started](#getting-started))
- Docker with Docker Compose — only needed once `compose.yaml` defines services
- A Microsoft Entra ID app registration of type **Web** with the redirect URIs
  `http://localhost:8080/login/oauth2/code/microsoft` and
  `http://localhost:8080/authenticate` (the post-logout redirect), and delegated
  Microsoft Graph scopes `openid`, `profile`, `email`

## Project Structure

```text
.
├── compose.yaml                    # Docker Compose services (currently empty)
├── pom.xml
├── mvnw / mvnw.cmd                 # Maven wrapper scripts
├── README.md
├── LOG.md                          # README change history
├── HELP.md                         # generated by Spring Initializr
└── src/
    ├── main/
    │   ├── java/com/pworks/web/app/pww/
    │   │   ├── ParraWorksWebApplication.java
    │   │   ├── security/
    │   │   │   ├── SecurityConfig.java
    │   │   │   └── SecurityConfigProps.java
    │   │   ├── service/
    │   │   │   └── AuthTokenService.java
    │   │   └── web/
    │   │       ├── AuthenticateController.java
    │   │       └── HomeController.java
    │   └── resources/
    │       ├── application.yaml
    │       ├── static/css/app.css
    │       └── templates/
    │           ├── authenticate.html
    │           └── home.html
    └── test/
        ├── java/com/pworks/web/app/pww/
        │   ├── ParraWorksWebApplicationTests.java
        │   ├── service/
        │   │   └── AuthTokenServiceTests.java
        │   └── web/PageControllerTests.java
        └── resources/
            └── application.yaml          # offline test config
```

## Getting Started

### Run

```bash
mvn spring-boot:run
```

The app starts on <http://localhost:8080>. `/` redirects to `/authenticate` until a
user signs in with Microsoft.

> The wrapper is currently incomplete: `.mvn/wrapper/maven-wrapper.properties` is
> missing, so `./mvnw` fails. Restore it with `mvn -N wrapper:wrapper` (or by
> re-exporting the project from Spring Initializr) and the wrapper commands below
> work:
>
> ```bash
> ./mvnw spring-boot:run
> ```

### Docker Compose

```bash
docker compose up --build
```

`compose.yaml` has no services yet, so this does not start the application. An app
service is added in a later milestone.

### Try it

1. Export `MICROSOFT_TENANT_ID`, `MICROSOFT_CLIENT_ID`, and
   `MICROSOFT_CLIENT_SECRET`.
2. Start the app and open <http://localhost:8080>.
3. You are redirected to `/authenticate`; choose **Sign in with Microsoft**.
4. Complete the Microsoft sign-in.
5. The home page shows your name, email, the ID token, and the access token, with
   the sign-out button above them.
6. Sign out, or wait 5 minutes for the session to expire, and you are sent back to
   the sign-in page. Signing out also ends the Microsoft session.

The Google button returns 404 until its registration is added.

## Configuration

`application.yaml` registers Microsoft with tenant-specific issuer discovery, sets
the session timeout, and lists the public routes:

```yaml
spring:
  application:
    name: pw-oauth-app
  security:
    oauth2:
      client:
        registration:
          microsoft:
            client-id: ${MICROSOFT_CLIENT_ID}
            client-secret: ${MICROSOFT_CLIENT_SECRET}
            authorization-grant-type: authorization_code
            scope:
              - openid
              - profile
              - email
            provider: microsoft
        provider:
          microsoft:
            user-name-attribute: sub
            issuer-uri: https://login.microsoftonline.com/${MICROSOFT_TENANT_ID}/v2.0
server:
  reactive:
    session:
      timeout: 5m
app:
  security:
    endpoints:
      allowed:
        - "/authenticate/**"
        - "/login/**"
        - "/css/**"
        - "/favicon.ico"
        - "/logout"
```

The `issuer-uri` makes the app fetch Microsoft's
[OpenID configuration](https://login.microsoftonline.com/common/v2.0/.well-known/openid-configuration)
for the tenant at startup, so the tenant, client id, and secret must all be set
before the app can start. `authorization-grant-type` is required: Spring Boot does
not default it and startup fails without it. The redirect URI defaults to
`{baseUrl}/login/oauth2/code/{registrationId}`.

`app.security.endpoints.allowed` is bound by `SecurityConfigProps` and passed to
`SecurityConfig` as the `permitAll` path list; add a path there instead of editing
the filter chain. `/login/**` must stay listed for the OAuth redirect URI, and
`/logout` is listed so the sign-out POST is permitted by the authorization rules;
CSRF still protects the form.

| Variable | Description |
| --- | --- |
| `MICROSOFT_TENANT_ID` | Entra ID tenant ID, GUID or `common` |
| `MICROSOFT_CLIENT_ID` | Microsoft Entra ID application (client) ID |
| `MICROSOFT_CLIENT_SECRET` | Microsoft client secret value |

Tests do not need any of these: `src/test/resources/application.yaml` replaces the
main file on the test classpath with dummy endpoints and its own
`app.security.endpoints.allowed` list, so the suite runs offline without secrets or
issuer discovery. Keep that file in sync when the main configuration changes.
Google and Apple will be added the same way:

| Variable | Planned for |
| --- | --- |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | Google |
| `APPLE_CLIENT_ID`, `APPLE_TEAM_ID`, `APPLE_KEY_ID`, `APPLE_PRIVATE_KEY` | Apple |

> Never commit client secrets, keys, or tokens. Use environment variables or a
> git-ignored local file.

## Session Behavior

- Created on successful Microsoft sign-in; holds the OIDC principal (name, email,
  claims) and the authorized client with the ID and access tokens.
- Expires after 5 minutes, set by `server.reactive.session.timeout=5m` in
  `application.yaml`.
- Stored in memory only: sessions are lost on restart and are not shared across
  instances.
- Anonymous or expired sessions are redirected to `/authenticate`.
- Sign-out uses `OidcClientInitiatedServerLogoutSuccessHandler`: it clears the local
  session, sends the browser to Microsoft's end-session endpoint, and returns to
  `{baseUrl}/authenticate`. Register `http://localhost:8080/authenticate` as a
  redirect URI in Entra ID so the post-logout redirect is accepted.
- Extending a session from a frontend dialog is planned; no endpoint exists yet.

## Testing

```bash
mvn test
```

Eight tests exist:

- `ParraWorksWebApplicationTests.contextLoads()` boots the full context.
- `PageControllerTests` checks that `/authenticate` is public and offers both
  providers, that anonymous requests to `/` are redirected to `/authenticate`, and
  that an authenticated OIDC user sees name, email, the sign-out form, and the CSRF
  token.
- `AuthTokenServiceTests` covers both token lookups: a stored authorized client
  yields its access token, and a missing client or ID token value yields an empty
  publisher instead of a null.

Unit tests use JUnit 5, Mockito, and Reactor's `StepVerifier`. `spring-boot-starter-security-test`
supplies `mockOidcLogin` for WebTestClient. Integration tests with Testcontainers come
later, together with MongoDB and Redis.

## Roadmap

- Configure the Google and Apple registrations
- Session extension dialog on the frontend
- App service in `compose.yaml` for the containerized run
- MongoDB for user persistence, Redis for session cache
- Testcontainers integration tests
- Public GitHub repository with CI

## Contributing

The project is currently a local repository only. Branch, commit, and review
conventions will be documented before the first public push.

## License

To be decided before the repository is made public.
