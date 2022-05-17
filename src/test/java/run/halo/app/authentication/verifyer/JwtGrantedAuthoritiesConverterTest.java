package run.halo.app.authentication.verifyer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import run.halo.app.identity.authentication.verifier.JwtGrantedAuthoritiesConverter;

/**
 * Tests for {@link JwtGrantedAuthoritiesConverter}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class JwtGrantedAuthoritiesConverterTest {

    @Test
    public void setAuthorityPrefixWithNullThenException() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();
        assertThatIllegalArgumentException()
            .isThrownBy(() -> jwtGrantedAuthoritiesConverter.setAuthorityPrefix(null));
    }

    @Test
    public void convertWhenTokenHasScopeAttributeThenTranslatedToAuthorities() {
        Jwt jwt = TestJwts.jwt()
            .claim("scope", "message:read message:write")
            .build();
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();
        Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
        assertThat(authorities).containsExactly(new SimpleGrantedAuthority("SCOPE_message:read"),
            new SimpleGrantedAuthority("SCOPE_message:write"));
    }

    @Test
    @DisplayName("convert with custom authority prefix when token has scope attribute then "
        + "translated to authorities")
    public void convertWithCustomAuthorityPrefixThenTranslatedToAuthorities() {
        Jwt jwt = TestJwts.jwt()
            .claim("scope", "message:read message:write")
            .build();
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
        assertThat(authorities).containsExactly(new SimpleGrantedAuthority("ROLE_message:read"),
            new SimpleGrantedAuthority("ROLE_message:write"));
    }

    @Test
    @DisplayName("convert with blank as custom authority prefix when token has scope attribute "
        + "then translated to authorities")
    public void convertWithBlankAsCustomAuthorityPrefixThenTranslatedToAuthorities() {
        Jwt jwt = TestJwts.jwt()
            .claim("scope", "message:read message:write")
            .build();
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
        assertThat(authorities).containsExactly(new SimpleGrantedAuthority("message:read"),
            new SimpleGrantedAuthority("message:write"));
    }

    @Test
    public void convertWhenTokenHasEmptyScopeAttributeThenTranslatedToNoAuthorities() {
        Jwt jwt = TestJwts.jwt()
            .claim("scope", "")
            .build();
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();
        Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
        assertThat(authorities).isEmpty();
    }

    @Test
    public void convertWhenTokenHasScpAttributeThenTranslatedToAuthorities() {
        Jwt jwt = TestJwts.jwt()
            .claim("scp", List.of("message:read", "message:write"))
            .build();
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();
        Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
        assertThat(authorities).containsExactly(new SimpleGrantedAuthority("SCOPE_message:read"),
            new SimpleGrantedAuthority("SCOPE_message:write"));
    }

    @Test
    @DisplayName("convert when token has both scope and scp then scope"
        + " attribute is translated to authorities")
    public void convertWhenTokenHasBothScopeAndScp() {
        Jwt jwt = TestJwts.jwt()
            .claim("scp", List.of("message:read", "message:write"))
            .claim("scope", "missive:read missive:write")
            .build();
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();
        Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
        assertThat(authorities).containsExactly(new SimpleGrantedAuthority("SCOPE_missive:read"),
            new SimpleGrantedAuthority("SCOPE_missive:write"));
    }
}
