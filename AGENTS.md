# AGENTS.md

## What this repo is

PWorks login app: a reactive Spring Boot service designed to authenticate users via
multiple OAuth2 / OpenID Connect (OIDC) identity providers (Apple, Google, and Microsoft)
and manage a short-lived reactive session displaying the authenticated user's email and full name.

`README.md` describes the target design; the `Project Status` table in `README.md` is the authoritative statement of what currently exists. Most of the login flow is under active development.

Verified stack: Spring Boot 4.1.2, Java 25 (`<java.version>`), Maven, Spring WebFlux,
Thymeleaf, devtools, `spring-boot-docker-compose`. Base package
`com.pworks.web.app.pww`, main class `ParraWorksWebApplication`, artifact
`parra-works-web`.

## Commands

`mvn` is not on PATH and `./mvnw` is broken because
`.mvn/wrapper/maven-wrapper.properties` is missing. Use the Maven distribution that
is already downloaded:

```bash
MVN=$(find ~/.m2/wrapper/dists/apache-maven-3.10.0 -name mvn -type f | head -1)
"$MVN" test
"$MVN" spring-boot:run     # http://localhost:8080
```

- Run all tests: `"$MVN" test`
- Single test class: `"$MVN" test -Dtest=ParraWorksWebApplicationTests`
- Single test method: `"$MVN" test -Dtest=ParraWorksWebApplicationTests#contextLoads`
- `PageControllerTests` uses `mockOidcLogin()` from `spring-security-test`; it needs
  a mock-based `WebTestClient`, so the class uses default `@SpringBootTest` (MOCK
  environment) with `@AutoConfigureWebTestClient`, not `RANDOM_PORT`.
- To repair the wrapper permanently, restore the properties file with the
  `distributionUrl` for Maven 3.10.0; do not hand-edit `mvnw`.
- The JDK on PATH is 26 while the project targets 25. The build succeeds, but
  Mockito prints a self-attach warning; keep it in mind before upgrading Java.

## Spring Boot 4 & Reactive WebFlux Guidelines

- The OAuth2 login starter is `spring-boot-starter-security-oauth2-client`.
  `spring-boot-starter-oauth2-client` is deprecated in Spring Boot 4.x — always use
  the `security-` prefix.
- Test starters are modular per feature (`spring-boot-starter-webflux-test`,
  `spring-boot-starter-thymeleaf-test`, `spring-boot-starter-security-test`). Follow this pattern
  instead of adding legacy monolithic test starters.
- Security dependencies: `spring-boot-starter-security-oauth2-client` at runtime and
  `spring-boot-starter-security-test` in test scope.
- Reactive CSRF handling: WebFlux defers the CSRF token as a `Mono<CsrfToken>`. Thymeleaf
  cannot resolve deferred reactive tokens directly, so `SecurityConfig#csrfTokenViewFilter`
  resolves the token and exposes it under `_csrf` in the exchange attributes. Ensure this filter
  remains active for all state-modifying requests (e.g., logout form submissions).
- Configuration file is `application.yaml` (do not use `application.yml`).
- Test configuration isolation: `application.yaml` requires provider secrets via environment variables
  at startup (e.g., `MICROSOFT_TENANT_ID`, `MICROSOFT_CLIENT_ID`, `MICROSOFT_CLIENT_SECRET`).
  Tests run hermetically and offline via `src/test/resources/application.yaml`, which supplies mock
  endpoints and prevents network calls during testing.
- Permitted endpoints: The sign-in page is `/authenticate` (was `/login`). Public paths live in
  `application.yaml` under `app.security.endpoints.allowed`, bound by `SecurityConfigProps`
  (`@ConfigurationProperties(prefix = "app.security")`) and passed to `permitAll`; add new paths
  there, not in the filter chain. The list must include `/authenticate/**`, `/login/**` (required
  for OAuth2 redirect URI `/login/oauth2/code/{id}`), `/css/**`, `/favicon.ico`, and `/logout`.
  `@ConfigurationPropertiesScan` on `ParraWorksWebApplication` discovers new properties classes.
- OIDC logout: sign-out uses `OidcClientInitiatedServerLogoutSuccessHandler` with a post-logout
  redirect of `{baseUrl}/authenticate`, so `http://localhost:8080/authenticate` must be registered
  as a redirect URI in the provider's app registration.
- Reactive sessions: Use reactive `WebSession` exclusively. Session timeout is configured via
  `server.reactive.session.timeout: 5m`. Avoid any servlet `HttpSession` assumptions or blocking calls.
  Read OAuth2 / OIDC token values through `AuthTokenService`; controllers must not load authorized
  clients from the repository themselves.
- `spring-boot-docker-compose` automatically starts services in `compose.yaml` during `spring-boot:run`.
- Non-blocking reactive programming: Adhere strictly to Project Reactor idioms. Return `Mono` or `Flux`
  and never call blocking operations (`block()`, `blockFirst()`, `blockLast()`, `toStream()`) on reactive pipelines.

## Multi-Provider OAuth2 / OIDC Architecture

The application supports three identity providers: Apple, Google, and Microsoft. Each provider has unique claim structures and authorization flows:

1. **Google**:
   - Standard OIDC discovery via `issuer-uri: https://accounts.google.com`.
   - Requested scopes: `openid`, `profile`, `email`.
   - Standard claims: `name`, `email`, `sub`, `picture`.

2. **Microsoft (Entra ID / Azure AD)**:
   - Tenant-aware discovery via `issuer-uri: https://login.microsoftonline.com/${MICROSOFT_TENANT_ID}/v2.0`.
   - Requested scopes: `openid`, `profile`, `email`.
   - Claim caveats: Microsoft often omits the `email` claim in ID tokens and provides `preferred_username` or `upn` instead. The application must fallback to `preferred_username` when `email` is absent.

3. **Apple (Sign in with Apple)**:
   - Uses OAuth 2.0 / OIDC with client authentication generated via signed JWT (ES256 with private key `.p8`).
   - Discovery / endpoints: `issuer-uri: https://appleid.apple.com`.
   - Requested scopes: `name`, `email` (using `openid`, `name`, `email`).
   - Claim caveats: Apple only sends the user's full name and email in the initial authorization payload (`user` JSON string in `POST` response) and omits them in subsequent ID tokens. The user identity mapper must handle private relay emails (`@privaterelay.appleid.com`) and persist initial profile data.

## SOLID Principles & Design Patterns

All code contributions must adhere to clean code practices, SOLID principles, and proven design patterns:

- **Single Responsibility Principle (SRP)**:
  - Separate concerns cleanly across dedicated layers: Web Controllers (routing/view models), Security Configuration (filter chains/authorization rules), Provider Adapters (claim extraction and normalization), Session Services, and Persistence Repositories.
- **Open/Closed Principle (OCP)**:
  - Design provider integrations using extensible abstractions (e.g., `OAuth2UserAttributeMapper` or `ProviderUserAdapter`). Adding new identity providers must only require a new provider adapter implementation without altering core authentication or controller logic.
- **Liskov Substitution Principle (LSP)**:
  - All provider-specific user adapters or repository implementations must fulfill common interface contracts predictably without violating expected postconditions or throwing unexpected runtime exceptions.
- **Interface Segregation Principle (ISP)**:
  - Keep interfaces lean, focused, and purposeful. Components should only depend on the specific methods they require (e.g., separate user retrieval from session storage).
- **Dependency Inversion Principle (DIP)**:
  - High-level business logic must depend on abstractions (interfaces), not concrete implementations.
  - **Constructor injection only**: All Spring components must use explicit constructor injection. Field injection (`@Autowired` on fields) and static mutable state are strictly prohibited.
- **Design Patterns**:
  - **Strategy / Adapter Pattern**: Normalizes provider-specific OIDC claims and attributes into a canonical immutable domain representation (e.g., an `AuthenticatedUser` record).
  - **Factory Pattern**: Encapsulates provider-specific client registration or attribute mapper selection based on `registrationId`.
  - **Repository Pattern**: Abstracts session and user persistence, allowing seamless transitions from in-memory stores to Redis / MongoDB without affecting web controllers.
- **Modern Java 25 Idioms**:
  - Use Java `record` classes for immutable DTOs, domain models, and value objects.
  - Utilize pattern matching, sealed interfaces, and modern switch expressions where appropriate to enforce type safety and exhaustiveness.
  - Strong typing with `java.util.Optional` or sealed result types rather than returning raw `null` values.

## Testing Strategy & Best Practices

Testing is an essential requirement for every feature and bug fix:

- **Frameworks**: Standardize on JUnit 5 (Jupiter), AssertJ for fluent assertions, and Mockito for mock objects.
- **Unit Testing**:
  - Test individual components (mappers, adapters, service logic, controllers) in isolation using Mockito.
  - Use Project Reactor's `StepVerifier` to thoroughly test reactive pipelines, verifying item emissions, error signals, and completion without blocking.
- **Integration & Security Testing**:
  - Use `@SpringBootTest` with `@AutoConfigureWebTestClient` and `WebTestClient`.
  - Utilize Spring Security Test utilities (`mockOidcLogin()`, `mockOAuth2Login()`, `csrf()`, `mutateWith()`) to simulate authenticated and anonymous user interactions.
  - Verify security boundaries: public access to `/authenticate` and static assets, redirect of unauthenticated requests to `/authenticate`, CSRF token presence in HTML forms, and proper sign-out redirects.
- **Hermetic & Fast Execution**:
  - Ensure all automated tests run offline without external network dependencies. Keep `src/test/resources/application.yaml` synchronized with any new configuration properties.

## Documentation Standards

- **Javadoc**:
  - All public classes, interfaces, records, configuration beans, and public methods must have clear, comprehensive Javadoc documentation.
  - Document parameter descriptions (`@param`), return values (`@return`), thrown exceptions (`@throws`), and reactive behavior (Mono/Flux publishers).
- **Inline Documentation**:
  - Include concise comments explaining the "why" behind non-obvious implementations (such as provider-specific claim nuances, CSRF workarounds, or reactive operators).
- **Project Documentation Sync**:
  - `README.md`: Update the `Project Status` table and features list whenever a milestone lands. Do not document planned features as currently implemented.
  - `LOG.md`: Tracks all updates to `README.md`. Append a new row with timestamp, title, and summary whenever `README.md` is modified.
- **External References**:
  - Always verify APIs and configurations against official documentation:
    - [Spring Security Reactive OAuth2 Login](https://docs.spring.io/spring-security/reference/reactive/oauth2/login/index.html)
    - [Spring Boot Application Properties](https://docs.spring.io/spring-boot/appendix/application-properties/index.html)
    - [Google OpenID Connect](https://developers.google.com/identity/openid-connect/openid-connect)
    - [Microsoft Entra ID OAuth 2.0 / OIDC](https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-auth-code-flow)
    - [Sign in with Apple](https://developer.apple.com/documentation/signinwithapple)
