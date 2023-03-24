package run.halo.app.core.extension.reconciler;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.event.post.ReplyChangedEvent;
import run.halo.app.event.post.ReplyDeletedEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;

/**
 * Reconciler for {@link Reply}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class ReplyReconciler implements Reconciler<Reconciler.Request> {
    protected static final String FINALIZER_NAME = "reply-protection";

    private final ExtensionClient client;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Result reconcile(Request request) {
        client.fetch(Reply.class, request.name())
            .ifPresent(reply -> {
                if (reply.getMetadata().getDeletionTimestamp() != null) {
                    cleanUpResourcesAndRemoveFinalizer(request.name());
                    return;
                }

                addFinalizerIfNecessary(reply);
                // on reply created
                eventPublisher.publishEvent(new ReplyChangedEvent(this, reply));
            });
        return new Result(false, null);
    }

    private void cleanUpResourcesAndRemoveFinalizer(String replyName) {
        client.fetch(Reply.class, replyName).ifPresent(reply -> {
            if (reply.getMetadata().getFinalizers() != null) {
                reply.getMetadata().getFinalizers().remove(FINALIZER_NAME);
            }
            client.update(reply);

            // on reply removed
            eventPublisher.publishEvent(new ReplyDeletedEvent(this, reply));
        });
    }

    private void addFinalizerIfNecessary(Reply oldReply) {
        Set<String> finalizers = oldReply.getMetadata().getFinalizers();
        if (finalizers != null && finalizers.contains(FINALIZER_NAME)) {
            return;
        }
        client.fetch(Reply.class, oldReply.getMetadata().getName())
            .ifPresent(reply -> {
                Set<String> newFinalizers = reply.getMetadata().getFinalizers();
                if (newFinalizers == null) {
                    newFinalizers = new HashSet<>();
                    reply.getMetadata().setFinalizers(newFinalizers);
                }
                newFinalizers.add(FINALIZER_NAME);
                client.update(reply);
            });
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Reply())
            .build();
    }
}
