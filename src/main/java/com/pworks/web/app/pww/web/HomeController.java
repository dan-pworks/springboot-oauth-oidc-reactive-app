package com.pworks.web.app.pww.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Renders the authenticated home page.
 */
@Controller
public class HomeController {

	// shortcut: no security module yet, so name and email are absent from the
	// model; fill them from the OIDC principal when oauth2Login lands.
	@GetMapping("/")
	public String home() {
		return "home";
	}

}
