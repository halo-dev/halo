package run.halo.app.security.verification;

import java.time.Duration;
import java.time.Instant;
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
    public boolean isAvailable(User user) {
        var settings = TwoFactorUtils.getTwoFactorAuthSettings(user);
        return settings.isEmailVerified() || settings.isTotpConfigured();
    }
}
