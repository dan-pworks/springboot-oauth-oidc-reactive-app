package com.pworks.web.app.pww.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Renders the sign-in page. The buttons point at the Spring Security
 * authorization endpoints, so the OAuth 2.0 client module owns the actual flow.
 */
@Controller
public class LoginController {

	@GetMapping("/login")
	public String login() {
		return "login";
	}

}
