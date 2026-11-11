package com.pworks.web.app.pww.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.server.WebFilter;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Reactive security configuration.
 *
 * <p>The sign-in page and static assets are public; everything else requires an
 * authenticated session. Sign-in uses the OAuth 2.0 / OpenID Connect client
 * registration named {@code microsoft}, and sign-out returns the user to the
 * sign-in page.</p>
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	final private SecurityConfigProps secConfigProps;
	private final ReactiveClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(SecurityConfigProps securityConfigProps, ReactiveClientRegistrationRepository clientRegistrationRepository) {
        this.secConfigProps = securityConfigProps;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    /**
	 * Builds the security filter chain for the reactive application.
	 *
	 * @param http the reactive HTTP security builder supplied by Spring Security
	 * @return the filter chain that enforces authentication and OAuth 2.0 login
	 */
	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		final List<String> allowedEndpoints = secConfigProps.endpoints.get("allowed");

		return http
				.authorizeExchange(exchanges ->
						exchanges
								.pathMatchers(allowedEndpoints.toArray(new String[0])).permitAll()
								.anyExchange().authenticated()
				)
				.oauth2Login(oauth2 -> oauth2.loginPage("/authenticate"))
				.logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler()))
				.build();
	}

	/**
	 * Configures OpenID Connect RP-Initiated Logout to terminate both the
	 * local session and the upstream Microsoft Entra ID session.
	 */
	private ServerLogoutSuccessHandler oidcLogoutSuccessHandler() {
		final OidcClientInitiatedServerLogoutSuccessHandler logoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);

		// Sets the post-logout redirect URI to return back to /authenticate
		logoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/authenticate");

		return logoutSuccessHandler;
	}

	/**
	 * Exposes the deferred CSRF token to Thymeleaf as {@code _csrf}.
	 *
	 * <p>WebFlux stores the token as a {@code Mono} in the exchange attributes,
	 * which Thymeleaf cannot read directly, so the logout form would post without
	 * a token and be rejected. Resolving the {@code Mono} and storing the value
	 * under the attribute name Thymeleaf templates use fixes that.</p>
	 *
	 * <p>
	 *     You need CSRF protection in a Spring Boot OAuth2/OIDC client because OAuth2/OIDC is
	 *     an authentication/authorization framework, not a session security mechanism.
	 *     Once the OAuth2 dance finishes, the Spring Boot client logs the user in by creating a
	 *     standard, cookie-based HTTP session (JSESSIONID). Because your application relies on
	 *     cookies to identify the logged-in user on subsequent requests, it is inherently
	 *     vulnerable to Cross-Site Request Forgery (CSRF).
	 * </p>
	 *
	 * @return a filter that adds {@code _csrf} to the exchange attributes
	 */
	@Bean
	public WebFilter csrfTokenViewFilter() {
		return (exchange, chain) -> {
			final Mono<CsrfToken> token = exchange.getAttribute(CsrfToken.class.getName());
			if (token == null) {
				return chain.filter(exchange);
			}

			return token
					.doOnNext(resolved -> {
						final Map<String, Object> attributes = exchange.getAttributes();
						attributes.put("_csrf", resolved);
					})
					.then(chain.filter(exchange));
		};
	}

}
