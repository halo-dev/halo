package run.halo.app.security.verification;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebSession;
import run.halo.app.core.extension.User;
import run.halo.app.security.authentication.twofactor.TwoFactorUtils;

/** Session-scoped security verification (sudo mode) shared by sensitive operations. */
@Component
public class SecurityVerificationService {

    public static final String VERIFIED_AT_SESSION_KEY = "security-verification.verified-at";

    public static final Duration VERIFICATION_TTL = Duration.ofMinutes(30);

    /** Whether the session has passed security verification within the TTL. */
    public boolean isVerified(WebSession session) {
        var verifiedAt = session.getAttribute(VERIFIED_AT_SESSION_KEY);
        return verifiedAt instanceof Instant instant
                && instant.plus(VERIFICATION_TTL).isAfter(Instant.now());
    }

    /** Mark the session as security verified from now on. */
    public void markVerified(WebSession session) {
        session.getAttributes().put(VERIFIED_AT_SESSION_KEY, Instant.now());
    }

    /** Whether the user has any security verification method (verified email or TOTP). */
    public boolean hasVerificationMethod(User user) {
        var settings = TwoFactorUtils.getTwoFactorAuthSettings(user);
        return settings.isEmailVerified() || settings.isTotpConfigured();
    }

    /** The security verification methods available to the given user, in display order. */
    public List<SecurityVerificationMethod> availableMethods(User user) {
        var settings = TwoFactorUtils.getTwoFactorAuthSettings(user);
        var methods = new ArrayList<SecurityVerificationMethod>();
        if (settings.isEmailVerified()) {
            methods.add(new SecurityVerificationMethod("email", "security-verification_email"));
        }
        if (settings.isTotpConfigured()) {
            methods.add(new SecurityVerificationMethod("totp", "security-verification_totp"));
        }
        return methods;
    }

    /**
     * A security verification method.
     *
     * @param name method identifier, used in the URL and the tab label key {@code form.method.<name>}
     * @param fragmentTemplateName template fragment rendering this method's form
     */
    public record SecurityVerificationMethod(String name, String fragmentTemplateName) {}
}
