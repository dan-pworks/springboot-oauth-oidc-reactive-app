package com.pworks.web.app.pww.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.*;

/** Unit tests for {@link AuthTokenService}. */
@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTests {

    private static final String REGISTRATION_ID = "microsoft";
    private static final String ACCESS_TOKEN = "access-token-value";
    private static final String ID_TOKEN = "id-token-value";

    @Mock
    private ServerOAuth2AuthorizedClientRepository authClientRepo;

    @Mock
    private Authentication authentication;

    @Mock
    private ServerWebExchange exchange;

    @InjectMocks
    private AuthTokenService service;

    /** A stored authorized client yields its raw access token. */
    @Test
    void getAccessTokenReturnsTokenValue() {
        final OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                ACCESS_TOKEN,
                Instant.now(),
                Instant.now().plusSeconds(300));

        final OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);

        when(authorizedClient.getAccessToken()).thenReturn(accessToken);

        when(authClientRepo.loadAuthorizedClient(REGISTRATION_ID, authentication, exchange))
                .thenReturn(Mono.just(authorizedClient));

        StepVerifier.create(service.getAccessToken(REGISTRATION_ID, authentication, exchange))
                .expectNext(ACCESS_TOKEN)
                .verifyComplete();

        verify(authClientRepo).loadAuthorizedClient(REGISTRATION_ID, authentication, exchange);
    }

    /** No authorized client in the session means no access token. */
    @Test
    void getAccessTokenIsEmptyWithoutAuthorizedClient() {
        when(authClientRepo.loadAuthorizedClient(REGISTRATION_ID, authentication, exchange))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.getAccessToken(REGISTRATION_ID, authentication, exchange))
                .verifyComplete();
    }

    /** The ID token on the principal is returned as-is. */
    @Test
    void getIdTokenReturnsTokenValue() {
        final OidcIdToken idToken = new OidcIdToken(
                ID_TOKEN,
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("sub", "user-1"));

        final OidcUser user = mock(OidcUser.class);

        when(user.getIdToken()).thenReturn(idToken);

        StepVerifier.create(service.getIdTokenFromOidcUser(user))
                .expectNext(ID_TOKEN)
                .verifyComplete();
    }

    /** A missing token value produces an empty publisher instead of a null. */
    @Test
    void getIdTokenIsEmptyWhenValueIsMissing() {
        final OidcIdToken idToken = mock(OidcIdToken.class);
        when(idToken.getTokenValue()).thenReturn(null);

        final OidcUser user = mock(OidcUser.class);
        when(user.getIdToken()).thenReturn(idToken);

        StepVerifier.create(service.getIdTokenFromOidcUser(user))
                .verifyComplete();
    }
}
