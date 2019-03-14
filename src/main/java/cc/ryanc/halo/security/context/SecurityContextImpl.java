package cc.ryanc.halo.security.context;

import cc.ryanc.halo.security.authentication.Authentication;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Security context implementation.
 *
 * @author johnniang
 */
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SecurityContextImpl implements SecurityContext {

    private Authentication authentication;

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

}
