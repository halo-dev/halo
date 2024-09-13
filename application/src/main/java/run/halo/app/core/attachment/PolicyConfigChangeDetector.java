package run.halo.app.core.attachment;

import static run.halo.app.extension.index.query.QueryFactory.equal;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionMatcher;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequestQueue;

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
    private final GroupVersionKind attachmentGvk = GroupVersionKind.fromExtension(Attachment.class);
    private final ExtensionClient client;
    private final AttachmentUpdateTrigger attachmentUpdateTrigger;

    @Override
    public Result reconcile(Request request) {
        client.fetch(ConfigMap.class, request.name())
            .ifPresent(configMap -> {
                var labels = configMap.getMetadata().getLabels();
                if (labels == null || !labels.containsKey(Policy.POLICY_OWNER_LABEL)) {
                    return;
                }
                var policyName = labels.get(Policy.POLICY_OWNER_LABEL);
                var attachmentNames = client.indexedQueryEngine()
                    .retrieveAll(attachmentGvk, ListOptions.builder()
                            .andQuery(equal("spec.policyName", policyName))
                            .build(),
                        Sort.unsorted()
                    );
                attachmentUpdateTrigger.addAll(attachmentNames);
            });
        return Result.doNotRetry();
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

    @Component
    static class AttachmentUpdateTrigger implements Reconciler<String>, SmartLifecycle {
        private final RequestQueue<String> queue;

        private final Controller controller;

        private volatile boolean running = false;

        private final ExtensionClient client;

        public AttachmentUpdateTrigger(ExtensionClient client) {
            this.client = client;
            this.queue = new DefaultQueue<>(Instant::now);
            this.controller = this.setupWith(null);
        }

        @Override
        public Result reconcile(String name) {
            client.fetch(Attachment.class, name).ifPresent(attachment -> {
                var annotations = MetadataUtil.nullSafeAnnotations(attachment);
                annotations.put(POLICY_UPDATED_AT, Instant.now().toString());
                client.update(attachment);
            });
            return Result.doNotRetry();
        }

        void addAll(List<String> names) {
            for (String name : names) {
                queue.addImmediately(name);
            }
        }

        @Override
        public Controller setupWith(ControllerBuilder builder) {
            return new DefaultController<>(
                "PolicyChangeAttachmentUpdater",
                this,
                queue,
                null,
                Duration.ofMillis(100),
                Duration.ofMinutes(10)
            );
        }

        @Override
        public void start() {
            controller.start();
            running = true;
        }

        @Override
        public void stop() {
            running = false;
            controller.dispose();
        }

        @Override
        public boolean isRunning() {
            return running;
        }
    }
}
