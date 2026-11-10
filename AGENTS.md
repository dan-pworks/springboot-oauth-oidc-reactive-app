# AGENTS.md

## What this repo is

PWorks login app: a reactive Spring Boot service meant to sign users in through
Google, Microsoft, or Apple and show a short-lived session. `README.md` describes the
target design; the `Project Status` table there is the only reliable statement of
what actually exists. Most of the login flow is not built yet.

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

- Single test: `"$MVN" test -Dtest=ParraWorksWebApplicationTests`
- Single method: `"$MVN" test -Dtest=ParraWorksWebApplicationTests#contextLoads`
- The test suite passes today: one context-load test.
- To repair the wrapper permanently, restore the properties file with the
  `distributionUrl` for Maven 3.10.0; do not hand-edit `mvnw`.
- The JDK on PATH is 26 while the project targets 25. The build succeeds, but
  Mockito prints a self-attach warning; keep it in mind before upgrading Java.

## Spring Boot 4 traps

- The OAuth2 login starter is `spring-boot-starter-security-oauth2-client`.
  `spring-boot-starter-oauth2-client` still resolves but is deprecated in its 4.1.2
  POM — use the `security-` name.
- Test starters are per feature (`spring-boot-starter-webflux-test`,
  `spring-boot-starter-thymeleaf-test`). Follow that pattern instead of adding the
  old all-in-one `spring-boot-starter-test`.
- No security starter is on the classpath yet; OAuth properties bind to nothing
  until it is added.
- Session timeout property is `server.reactive.session.timeout` (present in the
  `spring-boot-web-server` metadata). Sessions are reactive `WebSession`, not
  servlet `HttpSession`; any servlet-style security example is wrong here.
- Configuration file is `application.yaml`; there is no `application.yml`.
- `spring-boot-docker-compose` starts `compose.yaml` services on every
  `spring-boot:run`. `compose.yaml` is empty now; whatever you add there also runs
  locally.
- Thymeleaf warns about a missing `classpath:/templates/` until
  `src/main/resources/templates/` exists; add it with the first view.

## Design constraints

- SOLID, constructor injection only, no field injection, no static mutable state.
- Keep identity-provider and session storage behind interfaces so Google,
  Microsoft, and Apple share one flow and so in-memory storage can be replaced by
  MongoDB/Redis without touching controllers.
- Bind configuration with `@ConfigurationProperties`; read secrets from environment
  variables only, never from committed YAML.
- Do not guess APIs, property names, or artifact coordinates. Confirm against
  official docs and keep the reference in the change description:
  - Spring Security reactive OAuth2 login:
    <https://docs.spring.io/spring-security/reference/reactive/oauth2/login/index.html>
  - Spring Boot application properties:
    <https://docs.spring.io/spring-boot/appendix/application-properties/index.html>
  - Google OpenID Connect:
    <https://developers.google.com/identity/openid-connect/openid-connect>
  - Microsoft Entra auth code flow:
    <https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-auth-code-flow>
  - Sign in with Apple:
    <https://developer.apple.com/documentation/signinwithapple>
- Every new behavior needs one test. Mockito for units now, Testcontainers once
  MongoDB and Redis arrive.

## Documentation

- `LOG.md` tracks `README.md` only: append a row with timestamp, title, and summary
  after any README change.
- Update the README `Project Status` table when a milestone lands; do not describe
  planned behavior as current.
