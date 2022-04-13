package run.halo.app.identity.authentication;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.JwtProperties;
import run.halo.app.infra.utils.HaloUtils;

/**
 * Jwt token utils.
 *
 * @author guqing
 * @date 2022-04-12
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtTokenProvider implements Serializable {

    private final JwtEncoder jwtEncoder;

    private final JwtDecoder jwtDecoder;

    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder,
        JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.jwtProperties = jwtProperties;
    }

    private JwtClaimsSet createJwt(Authentication authentication, Instant expireAt) {
        String scope = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));
        return JwtClaimsSet.builder()
            // JWT ID (jti)
            .id(HaloUtils.simpleUUID())
            // 签发者
            .issuer(StringUtils.defaultIfBlank(jwtProperties.getIssuerUri(),
                "https://halo.run"))
            .issuedAt(Instant.now())
            // Authentication#getName maps to the JWT’s sub property, if one is present.
            .subject(authentication.getName())
            // expiration time (exp) claim
            .expiresAt(expireAt)
            .claim("scope", scope)
            .build();
    }

    public AccessToken getToken(Authentication authentication) {
        Instant expireAt = Instant.now().plusMillis(SecurityConstant.EXPIRATION_TIME);
        JwtClaimsSet tokenClaimsSet = createJwt(authentication, expireAt);
        Jwt token = jwtEncoder.encode(JwtEncoderParameters.from(tokenClaimsSet));

        JwtClaimsSet refreshTokenClaimsSet =
            createJwt(authentication, expireAt.plus(30, ChronoUnit.MINUTES));
        Jwt refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(refreshTokenClaimsSet));

        AccessToken accessToken = new AccessToken(token);
        accessToken.setRefreshToken(refreshToken);
        accessToken.setExpiration(expireAt.toEpochMilli());
        accessToken.setTokenType(SecurityConstant.TOKEN_PREFIX);
        return accessToken;
    }

    public Jwt verify(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (JwtException e) {
            return null;
        }
    }
}
