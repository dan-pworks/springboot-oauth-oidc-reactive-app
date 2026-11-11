package com.pworks.web.app.pww.web;

import com.pworks.web.app.pww.service.AuthTokenService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Renders the authenticated home page.
 */
@Controller
public class HomeController {

	private final AuthTokenService authTokenService;

    public HomeController(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    /**
	 * Shows the signed-in user's name and email.
	 *
	 * <p>Microsoft often omits the {@code email} claim and sends
	 * {@code preferred_username} instead, so that claim is used as a fallback.</p>
	 *
	 * @param user  the OpenID Connect principal, or {@code null} when the resolver
	 *              cannot map it
	 * @param model the view model receiving {@code name} and {@code email}
	 * @return the home view name
	 */
	@GetMapping("/")
	public Mono<String> home(@AuthenticationPrincipal OidcUser user,
                             OAuth2AuthenticationToken auth,
                             ServerWebExchange exchange,
                             Model model) {
		// Validate input parameters are not null
		Objects.requireNonNull(user, "user must not be null");
		Objects.requireNonNull(auth, "auth must not be null");
		Objects.requireNonNull(exchange, "exchange must not be null");

		// Add user basic info into model
		model.addAttribute("name", user.getFullName());
		model.addAttribute("email", emailOf(user));

		// Extract Registration ID from auth token
		final String registrationId = auth.getAuthorizedClientRegistrationId();

		// Combine both Mono method calls from Service, populate model if no error(s),
		// Otherwise, return home view name with the localized error message in the model.
		// zip completes empty when either token is missing, so fall back to a blank value
		// instead of skipping the view entirely.
		return Mono.zip(
				authTokenService.getAccessToken(registrationId, auth, exchange).defaultIfEmpty(""),
				authTokenService.getIdTokenFromOidcUser(user).defaultIfEmpty(""),
				(accessToken, idToken) -> {
					model.addAttribute("accessToken", accessToken);
					model.addAttribute("idToken", idToken);
					return "home";
				})
				.onErrorResume(t -> {
					model.addAttribute("error", t.getLocalizedMessage());
					return Mono.just("home");
				});
	}

    /**
	 * Reads the email claim, falling back to {@code preferred_username}.
	 *
	 * @param user the OpenID Connect principal
	 * @return the user's email, or {@code null} when neither claim is present
	 */
	private @Nullable String emailOf(@Nonnull OidcUser user) {
		if (StringUtils.hasText(user.getEmail())) {
			return user.getEmail();
		}

		final Object preferredUsername = user.getClaims().get("preferred_username");

		return preferredUsername != null ? preferredUsername.toString() : null;
	}

}
