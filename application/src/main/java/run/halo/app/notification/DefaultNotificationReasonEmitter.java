package run.halo.app.notification;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.ReasonType;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.NotFoundException;

/**
 * A default {@link NotificationReasonEmitter} implementation.
 *
 * @author guqing
 * @since 2.10.0
 */
@Component
@RequiredArgsConstructor
public class DefaultNotificationReasonEmitter implements NotificationReasonEmitter {

    private final ReactiveExtensionClient client;

    @Override
    public Mono<Void> emit(String reasonType,
        Consumer<ReasonPayload.ReasonPayloadBuilder> builder) {
        Assert.notNull(reasonType, "Reason type must not be empty.");
        var reason = createReason(reasonType, buildReasonPayload(builder));
        return validateReason(reason)
            .then(Mono.defer(() -> client.create(reason)))
            .then();
    }

    Mono<Void> validateReason(Reason reason) {
        String reasonTypeName = reason.getSpec().getReasonType();
        return client.fetch(ReasonType.class, reasonTypeName)
            .switchIfEmpty(Mono.error(new NotFoundException(
                "ReasonType [" + reasonTypeName + "] not found, do you forget to register it?"))
            )
            .doOnNext(reasonType -> {
                var valueMap = reason.getSpec().getAttributes();
                nullSafeList(reasonType.getSpec().getProperties())
                    .forEach(property -> {
                        if (property.isOptional()) {
                            return;
                        }
                        if (valueMap.get(property.getName()) == null) {
                            throw new IllegalArgumentException(
                                "Reason property [" + property.getName() + "] is required.");
                        }
                    });
            })
            .then();
    }

    <T> List<T> nullSafeList(List<T> t) {
        return defaultIfNull(t, List.of());
    }

    Reason createReason(String reasonType, ReasonPayload reasonData) {
        Reason reason = new Reason();
        reason.setMetadata(new Metadata());
        reason.getMetadata().setGenerateName("reason-");
        reason.setSpec(new Reason.Spec());
        if (reasonData.getAuthor() != null) {
            reason.getSpec().setAuthor(reasonData.getAuthor().name());
        }
        reason.getSpec().setReasonType(reasonType);
        reason.getSpec().setSubject(reasonData.getSubject());

        var reasonAttributes = new ReasonAttributes();
        if (reasonData.getAttributes() != null) {
            reasonAttributes.putAll(reasonData.getAttributes());
        }
        reason.getSpec().setAttributes(reasonAttributes);
        return reason;
    }

    ReasonPayload buildReasonPayload(Consumer<ReasonPayload.ReasonPayloadBuilder> reasonData) {
        var builder = ReasonPayload.builder();
        reasonData.accept(builder);
        return builder.build();
    }
}
