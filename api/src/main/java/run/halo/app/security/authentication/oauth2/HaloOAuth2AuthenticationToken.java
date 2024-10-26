package run.halo.app.security.authentication.oauth2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Halo OAuth2 authentication token which combines {@link UserDetails} and original
 * {@link OAuth2AuthenticationToken}.
 *
 * @author johnniang
 * @since 2.20.0
 */
public class HaloOAuth2AuthenticationToken extends AbstractAuthenticationToken {

    @Getter
    private final UserDetails userDetails;

    @Getter
    private final OAuth2AuthenticationToken original;

    /**
     * Constructs an {@code HaloOAuth2AuthenticationToken} using {@link UserDetails} and original
     * {@link OAuth2AuthenticationToken}.
     *
     * @param userDetails the {@link UserDetails}
     * @param original the original {@link OAuth2AuthenticationToken}
     */
    public HaloOAuth2AuthenticationToken(UserDetails userDetails,
        OAuth2AuthenticationToken original) {
        super(combineAuthorities(userDetails, original));
        this.userDetails = userDetails;
        this.original = original;
        setAuthenticated(true);
    }

    @Override
    public String getName() {
        return userDetails.getUsername();
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        var originalAuthorities = super.getAuthorities();
        var userDetailsAuthorities = getUserDetails().getAuthorities();
        var authorities = new ArrayList<GrantedAuthority>(
            originalAuthorities.size() + userDetailsAuthorities.size()
        );
        authorities.addAll(originalAuthorities);
        authorities.addAll(userDetailsAuthorities);
        return Collections.unmodifiableList(authorities);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public OAuth2User getPrincipal() {
        return original.getPrincipal();
    }

    /**
     * Creates an authenticated {@link HaloOAuth2AuthenticationToken} using {@link UserDetails} and
     * original {@link OAuth2AuthenticationToken}.
     *
     * @param userDetails the {@link UserDetails}
     * @param original the original {@link OAuth2AuthenticationToken}
     * @return an authenticated {@link HaloOAuth2AuthenticationToken}
     */
    public static HaloOAuth2AuthenticationToken authenticated(
        UserDetails userDetails, OAuth2AuthenticationToken original
    ) {
        return new HaloOAuth2AuthenticationToken(userDetails, original);
    }

    private static Collection<? extends GrantedAuthority> combineAuthorities(
        UserDetails userDetails, OAuth2AuthenticationToken original) {
        var userDetailsAuthorities = userDetails.getAuthorities();
        var originalAuthorities = original.getAuthorities();
        var authorities = new ArrayList<GrantedAuthority>(
            originalAuthorities.size() + userDetailsAuthorities.size()
        );
        authorities.addAll(originalAuthorities);
        authorities.addAll(userDetailsAuthorities);
        return Collections.unmodifiableList(authorities);
    }

}
