package run.halo.app.identity.authentication;

import java.time.Instant;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import run.halo.app.infra.utils.HaloUtils;

/**
 * An {@link OAuth2TokenGenerator} that generates a {@link Jwt}
 * used for an {@link OAuth2AccessToken}.
 *
 * @author guqing
 * @see OAuth2TokenGenerator
 * @see Jwt
 * @see JwtEncoder
 * @see OAuth2AccessToken
 * @since 2.0.0
 */
public record JwtGenerator(JwtEncoder jwtEncoder) implements OAuth2TokenGenerator<Jwt> {
    /**
     * Constructs a {@code JwtGenerator} using the provided parameters.
     *
     * @param jwtEncoder the jwt encoder
     */
    public JwtGenerator {
        Assert.notNull(jwtEncoder, "jwtEncoder cannot be null");
    }

    @Nullable
    @Override
    public Jwt generate(OAuth2TokenContext context) {
        if (context.getTokenType() == null
            || (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())
            && !OAuth2TokenType.REFRESH_TOKEN.equals(context.getTokenType()))) {
            return null;
        }
        Instant issuedAt = Instant.now();

        ProviderSettings providerSettings = context.getProviderContext().providerSettings();

        Instant expiresAt;
        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            expiresAt = issuedAt.plus(providerSettings.getAccessTokenTimeToLive());
        } else {
            // refresh token
            expiresAt = issuedAt.plus(providerSettings.getRefreshTokenTimeToLive());
        }

        String issuer = null;
        if (context.getProviderContext() != null) {
            issuer = context.getProviderContext().getIssuer();
        }

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder();
        if (StringUtils.hasText(issuer)) {
            claimsBuilder.issuer(issuer);
        }
        claimsBuilder
            .subject(context.getPrincipal().getName())
            .issuedAt(issuedAt)
            .notBefore(issuedAt)
            .id(HaloUtils.simpleUUID())
            .expiresAt(expiresAt);
        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            claimsBuilder.notBefore(issuedAt);
            if (!CollectionUtils.isEmpty(context.getAuthorizedScopes())) {
                claimsBuilder.claim(OAuth2ParameterNames.SCOPE, context.getAuthorizedScopes());
            }
        }
        JwsHeader headers = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(headers, claimsBuilder.build()));
    }
}
