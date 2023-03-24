package run.halo.app.core.extension.reconciler;

import static run.halo.app.core.extension.RoleBinding.containsUser;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Lazy;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.RoleBinding.Subject;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.utils.JsonUtils;

@Slf4j
@Component
public class RoleBindingReconciler implements Reconciler<Request> {

    private final ExtensionClient client;

    public RoleBindingReconciler(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(RoleBinding.class, request.name()).ifPresent(roleBinding -> {
            // get all usernames;
            var usernames = roleBinding.getSubjects().stream()
                .filter(subject -> User.KIND.equals(subject.getKind()))
                .map(Subject::getName)
                .collect(Collectors.toSet());

            // get all role-bindings lazily
            var bindings =
                Lazy.of(() -> client.list(RoleBinding.class, containsUser(usernames), null));

            usernames.forEach(username -> {
                var roleNames = bindings.get().stream()
                    .filter(containsUser(username))
                    .map(RoleBinding::getRoleRef)
                    .filter(roleRef -> Objects.equals(roleRef.getKind(), Role.KIND))
                    .map(RoleBinding.RoleRef::getName)
                    .sorted()
                    // we have to use LinkedHashSet below to make sure the sorted above functional
                    .collect(Collectors.toCollection(LinkedHashSet::new));
                // we should update the role names even if the role names are empty
                client.fetch(User.class, username).ifPresent(user -> {
                    var annotations = user.getMetadata().getAnnotations();
                    if (annotations == null) {
                        annotations = new HashMap<>();
                    }
                    var oldAnnotations = Map.copyOf(annotations);
                    annotations.put(User.ROLE_NAMES_ANNO, JsonUtils.objectToJson(roleNames));
                    user.getMetadata().setAnnotations(annotations);
                    if (!Objects.deepEquals(oldAnnotations, annotations)) {
                        // update user
                        client.update(user);
                    }
                });
            });
        });
        return new Result(false, null);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new RoleBinding())
            .build();
    }

}
