package run.halo.app.core.attachment;

import static run.halo.app.extension.index.query.QueryFactory.equal;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionMatcher;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.ReactiveExtensionPaginatedOperator;

/**
 * <p>Detects changes to {@link ConfigMap} that are referenced by {@link Policy} and updates the
 * {@link Attachment} with the {@link Policy} reference to reflect the change.</p>
 * <p>Without this, the link to the attachment corresponding to the storage policy configuration
 * change may not be correctly updated and only the service can be restarted.</p>
 *
 * @author guqing
 * @since 2.20.0
 */
@Component
@RequiredArgsConstructor
public class PolicyConfigChangeDetector implements Reconciler<Reconciler.Request> {
    static final String POLICY_UPDATED_AT = "storage.halo.run/policy-updated-at";
    private final ExtensionClient client;
    private final ReactiveExtensionClient reactiveClient;
    private final ReactiveExtensionPaginatedOperator paginatedOperator;

    @Override
    public Result reconcile(Request request) {
        client.fetch(ConfigMap.class, request.name())
            .ifPresent(configMap -> {
                var labels = configMap.getMetadata().getLabels();
                if (labels == null || !labels.containsKey(Policy.POLICY_OWNER_LABEL)) {
                    return;
                }
                var policyName = labels.get(Policy.POLICY_OWNER_LABEL);
                paginatedOperator.list(Attachment.class, ListOptions.builder()
                        .andQuery(equal("spec.policyName", policyName))
                        .build()
                    )
                    .flatMap(this::updateAnnotation)
                    .then()
                    .block();
            });
        return Result.doNotRetry();
    }

    protected Mono<Attachment> updateAnnotation(Attachment attachment) {
        var updatedAt = Instant.now().toString();
        populatePolicyUpdatedAt(attachment, updatedAt);
        return reactiveClient.update(attachment)
            .onErrorResume(OptimisticLockingFailureException.class,
                e -> updateAttachmentWithRetry(
                    attachment.getMetadata().getName(),
                    a -> populatePolicyUpdatedAt(a, updatedAt)
                )
            );
    }

    private Mono<Attachment> updateAttachmentWithRetry(String name, Consumer<Attachment> consumer) {
        return Mono.defer(() -> reactiveClient.get(Attachment.class, name)
                .doOnNext(consumer)
                .flatMap(reactiveClient::update)
            )
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance));
    }

    private void populatePolicyUpdatedAt(Attachment attachment, String value) {
        var annotations = MetadataUtil.nullSafeAnnotations(attachment);
        annotations.put(POLICY_UPDATED_AT, value);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        ExtensionMatcher matcher = extension -> {
            var configMap = (ConfigMap) extension;
            var labels = configMap.getMetadata().getLabels();
            return labels != null && labels.containsKey(Policy.POLICY_OWNER_LABEL);
        };
        return builder
            .extension(new ConfigMap())
            .syncAllOnStart(false)
            .onAddMatcher(matcher)
            .onUpdateMatcher(matcher)
            .onDeleteMatcher(matcher)
            .build();
    }
}
