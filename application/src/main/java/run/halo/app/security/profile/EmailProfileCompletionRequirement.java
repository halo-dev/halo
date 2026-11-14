package run.halo.app.security.profile;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

@Component
@Order(0)
@RequiredArgsConstructor
class EmailProfileCompletionRequirement implements ProfileCompletionRequirement {

    private static final ProfileCompletionStep STEP = new ProfileCompletionStep(
            URI.create("/complete-profile"), URI.create("email-not-set"), "A verified email address is required.");

    private final SystemConfigFetcher systemConfigFetcher;

    private final UserService userService;

    @Override
    public Mono<ProfileCompletionStep> evaluate(String username) {
        return systemConfigFetcher
                .fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                .filter(SystemSetting.User::isMustVerifyEmailOnRegistration)
                .flatMap(setting -> userService.getUser(username))
                .filter(user -> StringUtils.isBlank(user.getSpec().getEmail())
                        || !user.getSpec().isEmailVerified())
                .map(user -> STEP);
    }
}
