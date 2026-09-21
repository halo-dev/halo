package run.halo.app.security.sudo;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;

/**
 * A sudo confirmation method registered as a Spring bean.
 *
 * @author johnniang
 * @since 2.27.0
 */
interface SudoVerificationProvider {

    /** Stable method name returned to the client, e.g. {@code totp} or {@code email}. */
    String method();

    /** Whether this method requires sending a one-time code before confirmation. */
    boolean sendable();

    /** Whether this method is currently available for the given user. */
    Mono<Boolean> supports(User user);

    /** Send a one-time code. Providers that are not sendable should error. */
    Mono<Void> sendCode(User user);

    /** Verify the submitted code for the given user. */
    Mono<Void> verify(User user, String code);

    /** Optional masked destination shown in the UI, such as a masked email. */
    default String maskedTarget(User user) {
        return null;
    }
}
