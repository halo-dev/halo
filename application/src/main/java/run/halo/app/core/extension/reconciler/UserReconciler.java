package run.halo.app.core.extension.reconciler;

import static run.halo.app.core.extension.User.GROUP;
import static run.halo.app.core.extension.User.KIND;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupKind;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.PathUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserReconciler implements Reconciler<Request> {
    private static final String FINALIZER_NAME = "user-protection";
    private final ExtensionClient client;
    private final ExternalUrlSupplier externalUrlSupplier;
    private final RoleService roleService;
    private final RetryTemplate retryTemplate = RetryTemplate.builder()
        .maxAttempts(20)
        .fixedBackoff(300)
        .retryOn(IllegalStateException.class)
        .build();

    @Override
    public Result reconcile(Request request) {
        client.fetch(User.class, request.name()).ifPresent(user -> {
            if (user.getMetadata().getDeletionTimestamp() != null) {
                cleanUpResourcesAndRemoveFinalizer(request.name());
                return;
            }

            addFinalizerIfNecessary(user);
            ensureRoleNamesAnno(request.name());
            updatePermalink(request.name());
        });
        return new Result(false, null);
    }

    private void ensureRoleNamesAnno(String name) {
        client.fetch(User.class, name).ifPresent(user -> {
            Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(user);
            Map<String, String> oldAnnotations = Map.copyOf(annotations);

            List<String> roleNames = listRoleNamesRef(name);
            annotations.put(User.ROLE_NAMES_ANNO, JsonUtils.objectToJson(roleNames));

            if (!oldAnnotations.equals(annotations)) {
                client.update(user);
            }
        });
    }

    List<String> listRoleNamesRef(String username) {
        var subject = new RoleBinding.Subject(KIND, username, GROUP);
        return roleService.listRoleRefs(subject)
            .filter(this::isRoleRef)
            .map(RoleBinding.RoleRef::getName)
            .distinct()
            .collectList()
            .blockOptional()
            .orElse(List.of());
    }

    private boolean isRoleRef(RoleBinding.RoleRef roleRef) {
        var roleGvk = new Role().groupVersionKind();
        var gk = new GroupKind(roleRef.getApiGroup(), roleRef.getKind());
        return gk.equals(roleGvk.groupKind());
    }

    private void updatePermalink(String name) {
        client.fetch(User.class, name).ifPresent(user -> {
            if (AnonymousUserConst.isAnonymousUser(name)) {
                // anonymous user is not allowed to have permalink
                return;
            }
            if (user.getStatus() == null) {
                user.setStatus(new User.UserStatus());
            }
            User.UserStatus status = user.getStatus();
            String oldPermalink = status.getPermalink();

            status.setPermalink(getUserPermalink(user));

            if (!StringUtils.equals(oldPermalink, status.getPermalink())) {
                client.update(user);
            }
        });
    }

    private String getUserPermalink(User user) {
        return externalUrlSupplier.get()
            .resolve(PathUtils.combinePath("authors", user.getMetadata().getName()))
            .normalize().toString();
    }

    private void addFinalizerIfNecessary(User oldUser) {
        Set<String> finalizers = oldUser.getMetadata().getFinalizers();
        if (finalizers != null && finalizers.contains(FINALIZER_NAME)) {
            return;
        }
        client.fetch(User.class, oldUser.getMetadata().getName())
            .ifPresent(user -> {
                Set<String> newFinalizers = user.getMetadata().getFinalizers();
                if (newFinalizers == null) {
                    newFinalizers = new HashSet<>();
                    user.getMetadata().setFinalizers(newFinalizers);
                }
                newFinalizers.add(FINALIZER_NAME);
                client.update(user);
            });
    }

    private void cleanUpResourcesAndRemoveFinalizer(String userName) {
        client.fetch(User.class, userName).ifPresent(user -> {
            // wait for dependent resources to be deleted
            deleteUserConnections(userName);

            // remove finalizer
            if (user.getMetadata().getFinalizers() != null) {
                user.getMetadata().getFinalizers().remove(FINALIZER_NAME);
            }
            client.update(user);
        });
    }

    void deleteUserConnections(String userName) {
        listConnectionsByUsername(userName).forEach(client::delete);
        // wait for user connection to be deleted
        retryTemplate.execute(callback -> {
            if (listConnectionsByUsername(userName).size() > 0) {
                throw new IllegalStateException("User connection is not deleted yet");
            }
            return null;
        });
    }

    List<UserConnection> listConnectionsByUsername(String username) {
        return client.list(UserConnection.class,
            connection -> connection.getSpec().getUsername().equals(username), null);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new User())
            .build();
    }

}
