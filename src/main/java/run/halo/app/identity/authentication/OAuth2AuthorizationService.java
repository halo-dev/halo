package run.halo.app.identity.authentication;

import java.time.Instant;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

/**
 * @author guqing
 * @since 2.0.0
 */
public class OAuth2AuthorizationService {
    OAuth2Authorization findByUsername(String username, OAuth2TokenType oAuth2TokenType) {
        return new OAuth2Authorization.Builder().id("id")
            .accessToken(new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "token", Instant.now(),
                Instant.now().plusMillis(123)))
            .refreshToken(
                new OAuth2RefreshToken("refresh_token", Instant.now()))
            .principalName("guqing")
            .build();
    }

    void save(OAuth2Authorization authorization) {
    }
}
