# Read Me First
The following was discovered as part of building this project:

* No Docker Compose services found. As of now, the application won't start! Please add at least one service to the `compose.yaml` file.

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.2/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.1.2/maven-plugin/build-image.html)
* [Spring Reactive Web](https://docs.spring.io/spring-boot/4.1.2/reference/web/reactive.html)
* [Spring Security Reactive OAuth 2.0 Login](https://docs.spring.io/spring-security/reference/reactive/oauth2/login/index.html)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/4.1.2/reference/using/devtools.html)
* [Thymeleaf](https://docs.spring.io/spring-boot/4.1.2/reference/web/reactive.html#web.reactive.webflux.template-engines)
* [Docker Compose Support](https://docs.spring.io/spring-boot/4.1.2/reference/features/dev-services.html#features.dev-services.docker-compose)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a Reactive RESTful Web Service](https://spring.io/guides/gs/reactive-rest-service/)
* [Handling Form Submission](https://spring.io/guides/gs/handling-form-submission/)

### Sign-in configuration
Microsoft is the only configured identity provider. Set `MICROSOFT_TENANT_ID`,
`MICROSOFT_CLIENT_ID`, and `MICROSOFT_CLIENT_SECRET` before starting the app, then
open <http://localhost:8080>. The sign-in page is `/authenticate`; the home page at
`/` shows the user's name and email plus the OIDC ID token and OAuth 2.0 access
token. Sign-out uses OIDC RP-initiated logout and returns to `/authenticate`, so
`http://localhost:8080/authenticate` must also be registered as a redirect URI in
the Microsoft Entra ID app registration.

Public routes are listed under `app.security.endpoints.allowed` in
`src/main/resources/application.yaml` and bound by `SecurityConfigProps`. The file
must stay `application.yaml`, not `application.yml`. Tests run offline against
`src/test/resources/application.yaml`, which supplies dummy provider endpoints and
its own public-route list.

Run the app with `mvn spring-boot:run`. The Maven wrapper is currently incomplete
(`.mvn/wrapper/maven-wrapper.properties` is missing), so `./mvnw` fails until that
file is restored.

### Docker Compose support
This project contains a Docker Compose file named `compose.yaml`.

However, no services were found. As of now, the application won't start!

Please make sure to add at least one service in the `compose.yaml` file.

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.
