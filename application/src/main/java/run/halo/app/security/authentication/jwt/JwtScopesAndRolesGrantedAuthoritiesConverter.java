package run.halo.app.security.authentication.jwt;

import static run.halo.app.security.authorization.AuthorityUtils.ANONYMOUS_ROLE_NAME;
import static run.halo.app.security.authorization.AuthorityUtils.AUTHENTICATED_ROLE_NAME;
import static run.halo.app.security.authorization.AuthorityUtils.ROLE_PREFIX;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import reactor.core.publisher.Flux;

/**
 * GrantedAuthorities converter for SCOPE_ and ROLE_ prefixes.
 *
 * @author johnniang
 */
public class JwtScopesAndRolesGrantedAuthoritiesConverter
        implements Converter<Jwt, Flux<GrantedAuthority>> {

    private final Converter<Jwt, Collection<GrantedAuthority>> delegate;

    public JwtScopesAndRolesGrantedAuthoritiesConverter() {
        delegate = new JwtGrantedAuthoritiesConverter();
    }

    @Override
    public Flux<GrantedAuthority> convert(Jwt jwt) {
        var grantedAuthorities = new ArrayList<GrantedAuthority>();

        // add default roles
        grantedAuthorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + ANONYMOUS_ROLE_NAME));
        grantedAuthorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + AUTHENTICATED_ROLE_NAME));

        var delegateAuthorities = delegate.convert(jwt);
        if (delegateAuthorities != null) {
            grantedAuthorities.addAll(delegateAuthorities);
        }
        var roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            roles.stream()
                .map(role -> ROLE_PREFIX + role)
                .map(SimpleGrantedAuthority::new)
                .forEach(grantedAuthorities::add);
        }

        return Flux.fromIterable(grantedAuthorities);
    }

}
