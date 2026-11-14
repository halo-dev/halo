package run.halo.app.security.authentication.emailcode;

import java.util.ArrayList;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.util.Assert;

/**
 * Pre-authentication token holding the email address and verification code for email-code login.
 *
 * <p>After successful authentication, the {@link EmailCodeReactiveAuthenticationManager} returns a standard
 * {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken} so that downstream handlers
 * work identically to password login.
 *
 * @author johnniang
 * @since 2.26.0
 */
public class EmailCodeAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    private final String email;

    private String code;

    /** Creates an unauthenticated token with the given email and code. */
    public EmailCodeAuthenticationToken(String email, String code) {
        super(new ArrayList<>());
        Assert.hasText(email, "Email must not be blank");
        this.email = email;
        this.code = code;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return this.code;
    }

    @Override
    public Object getPrincipal() {
        return this.email;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.code = null;
    }
}
