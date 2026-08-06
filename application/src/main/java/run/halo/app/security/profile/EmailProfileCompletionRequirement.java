package run.halo.app.security.profile;

import java.net.URI;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.User;
import run.halo.app.infra.SystemSetting;

@Component
@Order(0)
class EmailProfileCompletionRequirement implements ProfileCompletionRequirement {

    private static final ProfileCompletionStep STEP = new ProfileCompletionStep(
            URI.create("/complete-profile"), URI.create("email-not-set"), "A verified email address is required.");

    @Override
    public Optional<ProfileCompletionStep> evaluate(User user, SystemSetting.User setting) {
        if (setting.isMustVerifyEmailOnRegistration() && !user.getSpec().isEmailVerified()) {
            return Optional.of(STEP);
        }
        return Optional.empty();
    }
}
