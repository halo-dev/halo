package run.halo.app.identity.authentication.verifier;

import java.util.Collection;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.util.Assert;

/**
 * @author guqing
 * @since 2.0.0
 */
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();

    private String principalClaimName = JwtClaimNames.SUB;

    @Override
    public final AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = this.jwtGrantedAuthoritiesConverter.convert(jwt);

        String principalClaimValue = jwt.getClaimAsString(this.principalClaimName);
        return new JwtAuthenticationToken(jwt, authorities, principalClaimValue);
    }

    /**
     * Sets the {@link Converter Converter&lt;Jwt, Collection&lt;GrantedAuthority&gt;&gt;}
     * to use. Defaults to {@link JwtGrantedAuthoritiesConverter}.
     *
     * @param jwtGrantedAuthoritiesConverter The converter
     * @see JwtGrantedAuthoritiesConverter
     * @since 5.2
     */
    public void setJwtGrantedAuthoritiesConverter(
        Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter) {
        Assert.notNull(jwtGrantedAuthoritiesConverter,
            "jwtGrantedAuthoritiesConverter cannot be null");
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
    }

    /**
     * Sets the principal claim name. Defaults to {@link JwtClaimNames#SUB}.
     *
     * @param principalClaimName The principal claim name
     * @since 5.4
     */
    public void setPrincipalClaimName(String principalClaimName) {
        Assert.hasText(principalClaimName, "principalClaimName cannot be empty");
        this.principalClaimName = principalClaimName;
    }
}
