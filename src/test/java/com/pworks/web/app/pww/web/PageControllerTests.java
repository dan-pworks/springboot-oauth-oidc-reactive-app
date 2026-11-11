package com.pworks.web.app.pww.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

@SpringBootTest
@AutoConfigureWebTestClient
class PageControllerTests {

	/** HTTP client bound to the application under test. */
	@Autowired
	private WebTestClient client;

	/** The sign-in page is public and offers both provider buttons. */
	@Test
	void authenticatePageOffersBothProviders() {
		client.get().uri("/authenticate").exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.value(body -> assertThat(body)
						.contains("Sign in with Microsoft")
						.contains("Sign in with Google")
						.contains("/oauth2/authorization/microsoft")
						.contains("/oauth2/authorization/google"));
	}

	/** Anonymous requests to the home page are redirected to the sign-in page. */
	@Test
	void homePageRedirectsAnonymousUsersToSignIn() {
		client.get().uri("/").exchange()
				.expectStatus().isFound()
				.expectHeader().location("/authenticate");
	}

	/** A signed-in user sees their name, email, and the sign-out form. */
	@Test
	void homePageShowsSignedInUser() {
		client.mutateWith(mockOidcLogin().idToken(idToken ->
						idToken
								.subject("user-1")
								.claim("name", "John Doe")
								.claim("preferred_username", "john@example.com"))
				)
				.get().uri("/").exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.value(body -> assertThat(body)
						.contains("John Doe")
						.contains("john@example.com")
						.contains("Sign out")
						.contains("name=\"_csrf\""));
	}

}
