package run.halo.app.core.extension.reconciler;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import run.halo.app.content.permalinks.ExtensionLocator;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.router.PermalinkIndexDeleteCommand;
import run.halo.app.theme.router.PermalinkIndexUpdateCommand;

@Slf4j
@Component
@AllArgsConstructor
public class UserReconciler implements Reconciler<Request> {
    private static final String FINALIZER_NAME = "user-protection";
    private final ExtensionClient client;
    private final ApplicationEventPublisher eventPublisher;
    private final ExternalUrlSupplier externalUrlSupplier;

    @Override
    public Result reconcile(Request request) {
        client.fetch(User.class, request.name()).ifPresent(user -> {
            if (user.getMetadata().getDeletionTimestamp() != null) {
                cleanUpResourcesAndRemoveFinalizer(request.name());
                return;
            }

            addFinalizerIfNecessary(user);
            updatePermalink(request.name());
        });
        return new Result(false, null);
    }

    private void updatePermalink(String name) {
        client.fetch(User.class, name).ifPresent(user -> {
            if (AnonymousUserConst.isAnonymousUser(name)) {
                // anonymous user is not allowed to have permalink
                return;
            }

            final User oldUser = JsonUtils.deepCopy(user);
            if (user.getStatus() == null) {
                user.setStatus(new User.UserStatus());
            }
            User.UserStatus status = user.getStatus();
            status.setPermalink(getUserPermalink(user));

            ExtensionLocator extensionLocator = getExtensionLocator(name);
            eventPublisher.publishEvent(
                new PermalinkIndexUpdateCommand(this, extensionLocator, status.getPermalink()));

            if (!user.equals(oldUser)) {
                client.update(user);
            }
        });
    }

    private static ExtensionLocator getExtensionLocator(String name) {
        return new ExtensionLocator(GroupVersionKind.fromExtension(User.class), name,
            name);
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
            eventPublisher.publishEvent(
                new PermalinkIndexDeleteCommand(this, getExtensionLocator(userName)));

            if (user.getMetadata().getFinalizers() != null) {
                user.getMetadata().getFinalizers().remove(FINALIZER_NAME);
            }
            client.update(user);
        });
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new User())
            .build();
    }

}
