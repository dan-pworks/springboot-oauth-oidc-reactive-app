package com.pworks.web.app.pww.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.server.WebFilter;

import reactor.core.publisher.Mono;

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

	/**
	 * Builds the security filter chain for the reactive application.
	 *
	 * @param http the reactive HTTP security builder supplied by Spring Security
	 * @return the filter chain that enforces authentication and OAuth 2.0 login
	 */
	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http
				.authorizeExchange(exchanges ->
						exchanges
								.pathMatchers("/authenticate/**","/login/**", "/css/**", "/favicon.ico").permitAll()
								.anyExchange().authenticated()
				)
				.oauth2Login(oauth2 -> oauth2.loginPage("/authenticate"))
				.logout(logout -> logout.logoutSuccessHandler(redirectAfterSignOut()))
				.build();
	}

	/**
	 * Redirects to the sign-in page after a successful sign-out.
	 *
	 * <p>The default handler redirects to {@code /login?logout}, which no longer
	 * exists since the sign-in page moved to {@code /authenticate}.</p>
	 *
	 * @return a handler that redirects to the sign-in page
	 */
	private ServerLogoutSuccessHandler redirectAfterSignOut() {
		RedirectServerLogoutSuccessHandler handler = new RedirectServerLogoutSuccessHandler();
		handler.setLogoutSuccessUrl(java.net.URI.create("/authenticate"));
		return handler;
	}

	/**
	 * Exposes the deferred CSRF token to Thymeleaf as {@code _csrf}.
	 *
	 * <p>WebFlux stores the token as a {@code Mono} in the exchange attributes,
	 * which Thymeleaf cannot read directly, so the logout form would post without
	 * a token and be rejected. Resolving the {@code Mono} and storing the value
	 * under the attribute name Thymeleaf templates use fixes that.</p>
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
					.doOnNext(resolved -> exchange.getAttributes().put("_csrf", resolved))
					.then(chain.filter(exchange));
		};
	}

}
