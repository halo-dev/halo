package run.halo.app.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;

/**
 * Tests for {@link JwtClaimsSet}.
 *
 * @author guqing
 * @date 2022-04-14
 */
public class JwtClaimsSetTest {

    public static JwtClaimsSet.Builder jwtClaimsSet() {
        String issuer = "https://provider.com";
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(1, ChronoUnit.HOURS);

        return JwtClaimsSet.builder()
            .issuer(issuer)
            .subject("subject")
            .audience(Collections.singletonList("client-1"))
            .issuedAt(issuedAt)
            .notBefore(issuedAt)
            .expiresAt(expiresAt)
            .id("jti")
            .claim("custom-claim-name", "custom-claim-value");
    }

    @Test
    public void buildWhenClaimsEmptyThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> JwtClaimsSet.builder().build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("claims cannot be empty");
    }

    @Test
    public void buildWhenAllClaimsProvidedThenAllClaimsAreSet() {
        JwtClaimsSet expectedJwtClaimsSet = jwtClaimsSet().build();

        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
            .issuer(expectedJwtClaimsSet.getIssuer().toExternalForm())
            .subject(expectedJwtClaimsSet.getSubject())
            .audience(expectedJwtClaimsSet.getAudience())
            .issuedAt(expectedJwtClaimsSet.getIssuedAt())
            .notBefore(expectedJwtClaimsSet.getNotBefore())
            .expiresAt(expectedJwtClaimsSet.getExpiresAt())
            .id(expectedJwtClaimsSet.getId())
            .claims(claims -> claims.put("custom-claim-name", "custom-claim-value"))
            .build();

        assertThat(jwtClaimsSet.getIssuer()).isEqualTo(expectedJwtClaimsSet.getIssuer());
        assertThat(jwtClaimsSet.getSubject()).isEqualTo(expectedJwtClaimsSet.getSubject());
        assertThat(jwtClaimsSet.getAudience()).isEqualTo(expectedJwtClaimsSet.getAudience());
        assertThat(jwtClaimsSet.getIssuedAt()).isEqualTo(expectedJwtClaimsSet.getIssuedAt());
        assertThat(jwtClaimsSet.getNotBefore()).isEqualTo(expectedJwtClaimsSet.getNotBefore());
        assertThat(jwtClaimsSet.getExpiresAt()).isEqualTo(expectedJwtClaimsSet.getExpiresAt());
        assertThat(jwtClaimsSet.getId()).isEqualTo(expectedJwtClaimsSet.getId());
        assertThat(jwtClaimsSet.<String>getClaim("custom-claim-name")).isEqualTo(
            "custom-claim-value");
        assertThat(jwtClaimsSet.getClaims()).isEqualTo(expectedJwtClaimsSet.getClaims());
    }

    @Test
    public void fromWhenNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> JwtClaimsSet.from(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("claims cannot be null");
    }

    @Test
    public void fromWhenClaimsProvidedThenCopied() {
        JwtClaimsSet expectedJwtClaimsSet = jwtClaimsSet().build();
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.from(expectedJwtClaimsSet).build();
        assertThat(jwtClaimsSet.getClaims()).isEqualTo(expectedJwtClaimsSet.getClaims());
    }

    @Test
    public void claimWhenNameNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> JwtClaimsSet.builder().claim(null, "value"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("name cannot be empty");
    }

    @Test
    public void claimWhenValueNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> JwtClaimsSet.builder().claim("name", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("value cannot be null");
    }
}
