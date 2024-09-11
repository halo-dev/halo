package run.halo.app.core.attachment.reconciler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.attachment.PolicyTemplate;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;

@Component
@RequiredArgsConstructor
public class PolicyReconciler implements Reconciler<Reconciler.Request> {
    private final ExtensionClient client;

    @Override
    public Result reconcile(Request request) {
        client.fetch(Policy.class, request.name())
            .ifPresent(this::checkOwnerLabel);
        return Result.doNotRetry();
    }

    private void checkOwnerLabel(Policy policy) {
        var policyName = policy.getMetadata().getName();
        var configMapName = policy.getSpec().getConfigMapName();
        client.fetch(ConfigMap.class, configMapName)
            .ifPresent(configMap -> {
                populateOwnerLabel(configMap, policyName);
                client.update(configMap);
            });

        var templateName = policy.getSpec().getTemplateName();
        client.fetch(PolicyTemplate.class, templateName)
            .ifPresent(policyTemplate -> {
                populateOwnerLabel(policyTemplate, policyName);
                client.update(policyTemplate);
            });
    }

    private static void populateOwnerLabel(AbstractExtension extension, String policyName) {
        var labels = MetadataUtil.nullSafeLabels(extension);
        labels.put(Policy.POLICY_OWNER_LABEL, policyName);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Policy())
            // sync on start for compatible with previous data
            .syncAllOnStart(true)
            .build();
    }
}
