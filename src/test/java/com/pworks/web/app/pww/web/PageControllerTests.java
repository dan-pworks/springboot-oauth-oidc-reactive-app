package com.pworks.web.app.pww.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PageControllerTests {

	@Autowired
	private WebTestClient client;

	@Test
	void loginPageOffersBothProviders() {
		client.get().uri("/login").exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.value(body -> assertThat(body)
						.contains("Sign in with Microsoft")
						.contains("Sign in with Google")
						.contains("/oauth2/authorization/microsoft")
						.contains("/oauth2/authorization/google"));
	}

	@Test
	void homePageRendersUserInfoAndSignOut() {
		client.get().uri("/").exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.value(body -> assertThat(body)
						.contains("Name")
						.contains("Email")
						.contains("Sign out"));
	}

}
