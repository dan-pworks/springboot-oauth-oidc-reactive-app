package com.pworks.web.app.pww.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Renders the authenticated home page.
 */
@Controller
public class HomeController {

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
	public String home(@AuthenticationPrincipal OidcUser user, Model model) {
		if (user != null) {
			model.addAttribute("name", user.getFullName());
			model.addAttribute("email", emailOf(user));
		}
		return "home";
	}

	/**
	 * Reads the email claim, falling back to {@code preferred_username}.
	 *
	 * @param user the OpenID Connect principal
	 * @return the user's email, or {@code null} when neither claim is present
	 */
	private String emailOf(OidcUser user) {
		if (user.getEmail() != null) {
			return user.getEmail();
		}
		Object preferredUsername = user.getClaims().get("preferred_username");
		return preferredUsername != null ? preferredUsername.toString() : null;
	}

}
