package com.pworks.web.app.pww;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the PWorks login application.
 *
 * <p>A reactive Spring Boot application that signs users in with OpenID Connect
 * and shows a short-lived session.</p>
 */
@SpringBootApplication
public class ParraWorksWebApplication {

	/**
	 * Starts the embedded reactive web server.
	 *
	 * @param args command-line arguments passed to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(ParraWorksWebApplication.class, args);
	}

}
