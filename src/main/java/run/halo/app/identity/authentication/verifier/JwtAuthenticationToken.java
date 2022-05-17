package run.halo.app.identity.authentication.verifier;

import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Transient;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * An implementation of an {@link AbstractOAuth2TokenAuthenticationToken} representing a
 * {@link Jwt} {@code Authentication}.
 *
 * @author guqing
 * @see AbstractOAuth2TokenAuthenticationToken
 * @see Jwt
 * @since 2.0.0
 */
@Transient
public class JwtAuthenticationToken extends AbstractOAuth2TokenAuthenticationToken<Jwt> {
    private final String name;

    /**
     * Constructs a {@code JwtAuthenticationToken} using the provided parameters.
     *
     * @param jwt the JWT
     */
    public JwtAuthenticationToken(Jwt jwt) {
        super(jwt);
        this.name = jwt.getSubject();
    }

    /**
     * Constructs a {@code JwtAuthenticationToken} using the provided parameters.
     *
     * @param jwt the JWT
     * @param authorities the authorities assigned to the JWT
     */
    public JwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
        super(jwt, authorities);
        this.setAuthenticated(true);
        this.name = jwt.getSubject();
    }

    /**
     * Constructs a {@code JwtAuthenticationToken} using the provided parameters.
     *
     * @param jwt the JWT
     * @param authorities the authorities assigned to the JWT
     * @param name the principal name
     */
    public JwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities,
        String name) {
        super(jwt, authorities);
        this.setAuthenticated(true);
        this.name = name;
    }

    @Override
    public Map<String, Object> getTokenAttributes() {
        return this.getToken().getClaims();
    }

    /**
     * The principal name which is, by default, the {@link Jwt}'s subject.
     */
    @Override
    public String getName() {
        return this.name;
    }
}
