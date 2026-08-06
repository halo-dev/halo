package run.halo.app.security.profile;

import java.util.Optional;
import run.halo.app.core.extension.User;
import run.halo.app.infra.SystemSetting;

/** Determines whether a user has a pending profile-completion step. */
public interface ProfileCompletionRequirement {

    /**
     * Evaluates the requirement against the current user and settings.
     *
     * <p>Requirements are evaluated in Spring order and the first returned step wins.
     */
    Optional<ProfileCompletionStep> evaluate(User user, SystemSetting.User setting);
}
