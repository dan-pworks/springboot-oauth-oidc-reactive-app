package com.pworks.web.app.pww.service;

import jakarta.annotation.Nonnull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Exposes the tokens produced by the OAuth 2.0 / OpenID Connect login.
 *
 * <p>The home page needs the raw token strings, but the controller should not
 * reach into the authorized-client repository itself. This service keeps that
 * lookup in one place and returns reactive values so nothing blocks.</p>
 */
@Service
public class AuthTokenService {
    private final ServerOAuth2AuthorizedClientRepository authClientRepo;

    /**
     * Creates the service with the repository that stores authorized clients.
     *
     * @param authClientRepo per-session repository used to find the login's tokens
     */
    public AuthTokenService(ServerOAuth2AuthorizedClientRepository authClientRepo) {
        this.authClientRepo = authClientRepo;
    }

    /**
     * Loads the access token for the current sign-in.
     *
     * <p>Spring Security keeps the token with the authorized client, not on the
     * principal, so it has to be looked up from the session on each request. The
     * result is empty when the session has no authorized client, for example
     * after expiry or in a mocked login.</p>
     *
     * @param clientRegistrationId id of the provider registration, such as {@code microsoft}
     * @param authentication       current user authentication
     * @param exchange             request used to resolve the session
     * @return a publisher with the raw access token, or an empty publisher when none is stored
     */
    public @Nonnull Mono<String> getAccessToken(@Nonnull String clientRegistrationId,
                                                @Nonnull Authentication authentication,
                                                @Nonnull ServerWebExchange exchange) {
        return authClientRepo
                .loadAuthorizedClient(clientRegistrationId, authentication, exchange)
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue);
    }

    /**
     * Returns the ID token that came back with the sign-in.
     *
     * <p>The token is already parsed on the {@link OidcUser}; the page only needs
     * its compact string form.</p>
     *
     * @param user signed-in OpenID Connect user
     * @return a publisher with the raw ID token, or an empty publisher when the value is missing
     */
    public @Nonnull Mono<String> getIdTokenFromOidcUser(@Nonnull OidcUser user) {
        final OidcIdToken idToken = user.getIdToken();

        return Mono.justOrEmpty(idToken.getTokenValue());
    }
}
