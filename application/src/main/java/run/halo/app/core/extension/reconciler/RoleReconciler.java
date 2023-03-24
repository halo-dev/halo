package run.halo.app.core.extension.reconciler;

import static java.util.Objects.deepEquals;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Role;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;

/**
 * Role reconcile.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class RoleReconciler implements Reconciler<Request> {

    private final ExtensionClient client;

    public RoleReconciler(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(Role.class, request.name())
            .ifPresent(role -> {
                Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(role);
                // override dependency rules to annotations
                annotations.put(Role.ROLE_DEPENDENCY_RULES, "[]");
                annotations.put(Role.UI_PERMISSIONS_AGGREGATED_ANNO, "[]");

                updateLabelsAndAnnotations(role);
            });
        return new Result(false, null);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Role())
            .build();
    }

    private void updateLabelsAndAnnotations(Role role) {
        var annotations = role.getMetadata().getAnnotations();
        var labels = role.getMetadata().getLabels();
        client.fetch(Role.class, role.getMetadata().getName())
            .filter(freshRole -> !deepEquals(annotations, freshRole.getMetadata().getAnnotations())
            || deepEquals(labels, freshRole.getMetadata().getLabels()))
            .ifPresent(freshRole -> {
                freshRole.getMetadata().setAnnotations(annotations);
                freshRole.getMetadata().setLabels(labels);
                client.update(freshRole);
            });
    }
}
