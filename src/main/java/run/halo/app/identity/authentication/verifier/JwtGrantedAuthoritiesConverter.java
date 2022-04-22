package run.halo.app.identity.authentication.verifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Extracts the {@link GrantedAuthority}s from scope attributes typically found in a
 * {@link Jwt}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class JwtGrantedAuthoritiesConverter implements
    Converter<Jwt, Collection<GrantedAuthority>> {
    private static final String DEFAULT_AUTHORITY_PREFIX = "SCOPE_";

    private static final Collection<String> WELL_KNOWN_AUTHORITIES_CLAIM_NAMES =
        List.of("scope", "scp");

    private String authorityPrefix = DEFAULT_AUTHORITY_PREFIX;

    private String authoritiesClaimName;

    /**
     * Extract {@link GrantedAuthority}s from the given {@link Jwt}.
     *
     * @param jwt The {@link Jwt} token
     * @return The {@link GrantedAuthority authorities} read from the token scopes
     */
    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String authority : getAuthorities(jwt)) {
            grantedAuthorities.add(new SimpleGrantedAuthority(this.authorityPrefix + authority));
        }
        return grantedAuthorities;
    }

    /**
     * Sets the prefix to use for {@link GrantedAuthority authorities} mapped by this
     * converter. Defaults to
     * {@link JwtGrantedAuthoritiesConverter#DEFAULT_AUTHORITY_PREFIX}.
     *
     * @param authorityPrefix The authority prefix
     * @since 5.2
     */
    public void setAuthorityPrefix(String authorityPrefix) {
        Assert.notNull(authorityPrefix, "authorityPrefix cannot be null");
        this.authorityPrefix = authorityPrefix;
    }

    /**
     * Sets the name of token claim to use for mapping {@link GrantedAuthority
     * authorities} by this converter. Defaults to
     * {@link JwtGrantedAuthoritiesConverter#WELL_KNOWN_AUTHORITIES_CLAIM_NAMES}.
     *
     * @param authoritiesClaimName The token claim name to map authorities
     * @since 5.2
     */
    public void setAuthoritiesClaimName(String authoritiesClaimName) {
        Assert.hasText(authoritiesClaimName, "authoritiesClaimName cannot be empty");
        this.authoritiesClaimName = authoritiesClaimName;
    }

    private String getAuthoritiesClaimName(Jwt jwt) {
        if (this.authoritiesClaimName != null) {
            return this.authoritiesClaimName;
        }
        for (String claimName : WELL_KNOWN_AUTHORITIES_CLAIM_NAMES) {
            if (jwt.hasClaim(claimName)) {
                return claimName;
            }
        }
        return null;
    }

    private Collection<String> getAuthorities(Jwt jwt) {
        String claimName = getAuthoritiesClaimName(jwt);
        if (claimName == null) {
            log.trace(
                "Returning no authorities since could not find any claims that might contain "
                    + "scopes");
            return Collections.emptyList();
        }
        log.trace("Looking for scopes in claim [{}]", claimName);
        Object authorities = jwt.getClaim(claimName);
        if (authorities instanceof String) {
            if (StringUtils.hasText((String) authorities)) {
                return Arrays.asList(((String) authorities).split(" "));
            }
            return Collections.emptyList();
        }
        if (authorities instanceof Collection) {
            return castAuthoritiesToCollection(authorities);
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private Collection<String> castAuthoritiesToCollection(Object authorities) {
        return (Collection<String>) authorities;
    }
}
