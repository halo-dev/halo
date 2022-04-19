package run.halo.app.authentication;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.util.CollectionUtils;
import run.halo.app.identity.authentication.OAuth2Authorization;

/**
 * @author guqing
 * @since 2.0.0
 */
public class TestOAuth2Authorizations {
    public static OAuth2Authorization.Builder authorization() {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER, "access-token", Instant.now(),
            Instant.now().plusSeconds(300));
        return authorization(accessToken, Collections.emptyMap());
    }

    private static OAuth2Authorization.Builder authorization(
        OAuth2AccessToken accessToken, Map<String, Object> accessTokenClaims) {
        OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
            "refresh-token", Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS));
        return new OAuth2Authorization.Builder()
            .id("id")
            .principalName("principal")
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .token(accessToken, (metadata) -> metadata.putAll(tokenMetadata(accessTokenClaims)))
            .refreshToken(refreshToken)
            .attribute(Principal.class.getName(),
                new TestingAuthenticationToken("principal", "123456", "ROLE_A", "ROLE_B"))
            .attribute(OAuth2Authorization.AUTHORIZED_SCOPE_ATTRIBUTE_NAME, Collections.emptySet());
    }

    private static Map<String, Object> tokenMetadata(Map<String, Object> tokenClaims) {
        Map<String, Object> tokenMetadata = new HashMap<>();
        tokenMetadata.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, false);
        if (CollectionUtils.isEmpty(tokenClaims)) {
            tokenClaims = defaultTokenClaims();
        }
        tokenMetadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, tokenClaims);
        return tokenMetadata;
    }

    private static Map<String, Object> defaultTokenClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("claim1", "value1");
        claims.put("claim2", "value2");
        claims.put("claim3", "value3");
        return claims;
    }
}
