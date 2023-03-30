package run.halo.app.core.extension.reconciler;

import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.AuthProvider;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.security.AuthProviderService;

/**
 * Reconciler for {@link AuthProvider}.
 *
 * @author guqing
 * @since 2.4.0
 */
@Component
@RequiredArgsConstructor
public class AuthProviderReconciler implements Reconciler<Reconciler.Request> {
    private static final String FINALIZER_NAME = "auth-provider-protection";
    private final ExtensionClient client;
    private final AuthProviderService authProviderService;

    @Override
    public Result reconcile(Request request) {
        client.fetch(AuthProvider.class, request.name())
            .ifPresent(authProvider -> {
                if (authProvider.getMetadata().getDeletionTimestamp() != null) {
                    removeFinalizer(request.name());
                    return;
                }
                addFinalizerIfNecessary(authProvider);
                handlePrivileged(authProvider);
            });
        return Result.doNotRetry();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new AuthProvider())
            .build();
    }

    private void handlePrivileged(AuthProvider authProvider) {
        if (privileged(authProvider)) {
            authProviderService.enable(authProvider.getMetadata().getName()).block();
        }
    }

    private void addFinalizerIfNecessary(AuthProvider oldAuthProvider) {
        Set<String> finalizers = oldAuthProvider.getMetadata().getFinalizers();
        if (finalizers != null && finalizers.contains(FINALIZER_NAME)) {
            return;
        }
        client.fetch(AuthProvider.class, oldAuthProvider.getMetadata().getName())
            .ifPresent(authProvider -> {
                Set<String> newFinalizers = authProvider.getMetadata().getFinalizers();
                if (newFinalizers == null) {
                    newFinalizers = new HashSet<>();
                    authProvider.getMetadata().setFinalizers(newFinalizers);
                }
                newFinalizers.add(FINALIZER_NAME);
                client.update(authProvider);
            });
    }

    private void removeFinalizer(String authProviderName) {
        client.fetch(AuthProvider.class, authProviderName)
            .ifPresent(authProvider -> {
                // Disable auth provider
                authProviderService.disable(authProviderName).block();

                if (authProvider.getMetadata().getFinalizers() != null) {
                    authProvider.getMetadata().getFinalizers().remove(FINALIZER_NAME);
                }
                client.update(authProvider);
            });
    }

    private boolean privileged(AuthProvider authProvider) {
        return BooleanUtils.TRUE.equals(MetadataUtil.nullSafeLabels(authProvider)
            .get(AuthProvider.PRIVILEGED_LABEL));
    }
}
