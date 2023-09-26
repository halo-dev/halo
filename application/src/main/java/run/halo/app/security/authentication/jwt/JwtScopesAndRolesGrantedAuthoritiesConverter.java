package run.halo.app.security.authentication.jwt;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import run.halo.app.security.authorization.AuthorityUtils;

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
        var delegateAuthorities = delegate.convert(jwt);
        if (delegateAuthorities != null) {
            grantedAuthorities.addAll(delegateAuthorities);
        }
        var roles = jwt.getClaimAsStringList("roles");
        if (!CollectionUtils.isEmpty(roles)) {
            roles.stream()
                .map(role -> AuthorityUtils.ROLE_PREFIX + role)
                .map(SimpleGrantedAuthority::new)
                .forEach(grantedAuthorities::add);
        }
        return Flux.fromIterable(grantedAuthorities);
    }

}
