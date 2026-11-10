package com.pworks.web.app.pww.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Renders the sign-in page. The buttons point at the Spring Security
 * authorization endpoints, so the OAuth 2.0 client module owns the actual flow.
 */
@Controller
public class AuthenticateController {

	/**
	 * Shows the sign-in page.
	 *
	 * @return the login view name
	 */
	@GetMapping("/authenticate")
	public String login() {
		return "authenticate";
	}

}
