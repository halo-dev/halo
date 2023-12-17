package run.halo.app.notification;

import java.util.function.Consumer;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.Reason;

/**
 * {@link NotificationReasonEmitter} to emit notification reason.
 *
 * @author guqing
 * @since 2.10.0
 */
public interface NotificationReasonEmitter {

    /**
     * Emit a {@link Reason} with {@link ReasonPayload}.
     *
     * @param reasonType reason type to emitter must not be blank
     * @param reasonData reason data must not be null
     */
    Mono<Void> emit(String reasonType, Consumer<ReasonPayload.ReasonPayloadBuilder> reasonData);
}
