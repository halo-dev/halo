package run.halo.app.security.profile;

import reactor.core.publisher.Mono;

/** Determines whether a user has a pending profile-completion step. */
public interface ProfileCompletionRequirement {

    /**
     * Evaluates the requirement for the current user.
     *
     * <p>Requirements are evaluated in Spring order and the first returned step wins.
     */
    Mono<ProfileCompletionStep> evaluate(String username);
}
