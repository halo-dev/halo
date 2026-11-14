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

    /** Whether this method sends a one-time code, so the client should offer a "send code" action. */
    boolean canSendCode();

    /** Whether this method is currently available for the given user. */
    Mono<Boolean> supports(User user);

    /** Send a one-time code. Providers that cannot send a code should error. */
    Mono<Void> sendCode(User user);

    /** Verify the submitted code for the given user. */
    Mono<Void> verify(User user, String code);

    /** Optional masked destination shown in the UI, such as a masked email. */
    default String maskedTarget(User user) {
        return null;
    }
}
