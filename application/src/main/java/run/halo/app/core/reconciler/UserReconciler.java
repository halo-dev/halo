package run.halo.app.core.reconciler;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.defaultSort;
import static run.halo.app.extension.ExtensionUtil.isDeleted;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;
import static run.halo.app.extension.index.query.QueryFactory.equal;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.extension.controller.RequeueException;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.utils.JsonUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserReconciler implements Reconciler<Request> {
    private static final String FINALIZER_NAME = "user-protection";
    private final ExtensionClient client;
    private final ExternalUrlSupplier externalUrlSupplier;
    private final RoleService roleService;
    private final AttachmentService attachmentService;
    private final UserService userService;

    @Override
    public Result reconcile(Request request) {
        client.fetch(User.class, request.name()).ifPresent(user -> {
            if (isDeleted(user)) {
                deleteUserConnections(request.name());
                removeFinalizers(user.getMetadata(), Set.of(FINALIZER_NAME));
                client.update(user);
                return;
            }
            addFinalizers(user.getMetadata(), Set.of(FINALIZER_NAME));
            ensureRoleNamesAnno(user);
            updatePermalink(user);
            handleAvatar(user);
            checkVerifiedEmail(user);
            client.update(user);
        });
        return new Result(false, null);
    }

    private void checkVerifiedEmail(User user) {
        var username = user.getMetadata().getName();
        if (!user.getSpec().isEmailVerified()) {
            return;
        }
        var email = user.getSpec().getEmail();
        if (StringUtils.isBlank(email)) {
            return;
        }
        if (checkEmailInUse(username, email)) {
            user.getSpec().setEmailVerified(false);
        }
    }

    private Boolean checkEmailInUse(String username, String email) {
        return userService.listByEmail(email)
            .filter(existUser -> existUser.getSpec().isEmailVerified())
            .filter(existUser -> !existUser.getMetadata().getName().equals(username))
            .hasElements()
            .blockOptional()
            .orElse(false);
    }

    private void handleAvatar(User user) {
        var annotations = Optional.ofNullable(user.getMetadata().getAnnotations())
            .orElseGet(HashMap::new);
        user.getMetadata().setAnnotations(annotations);

        var avatarAttachmentName = annotations.get(User.AVATAR_ATTACHMENT_NAME_ANNO);
        var oldAvatarAttachmentName =
            annotations.get(User.LAST_AVATAR_ATTACHMENT_NAME_ANNO);
        // remove old avatar if needed
        if (StringUtils.isNotBlank(oldAvatarAttachmentName)
            && !StringUtils.equals(avatarAttachmentName, oldAvatarAttachmentName)) {
            client.fetch(Attachment.class, oldAvatarAttachmentName)
                .ifPresent(client::delete);
            annotations.remove(User.LAST_AVATAR_ATTACHMENT_NAME_ANNO);
        }

        var spec = user.getSpec();
        if (StringUtils.isBlank(avatarAttachmentName)) {
            if (StringUtils.isNotBlank(spec.getAvatar())) {
                log.info("Remove avatar for user({})", user.getMetadata().getName());
            }
            spec.setAvatar(null);
            return;
        }
        client.fetch(Attachment.class, avatarAttachmentName)
            .flatMap(attachment -> attachmentService.getPermalink(attachment)
                .blockOptional(Duration.ofMinutes(1))
            )
            .map(URI::toString)
            .ifPresentOrElse(avatar -> {
                if (!Objects.equals(avatar, spec.getAvatar())) {
                    log.info(
                        "Update avatar for user({}) to {}",
                        user.getMetadata().getName(), avatar
                    );
                }
                spec.setAvatar(avatar);
                // reset last avatar
                annotations.put(
                    User.LAST_AVATAR_ATTACHMENT_NAME_ANNO,
                    avatarAttachmentName
                );
            }, () -> {
                throw new RequeueException(
                    new Result(true, null),
                    "Avatar permalink(%s) is not available yet."
                        .formatted(avatarAttachmentName)
                );
            });
    }

    private void ensureRoleNamesAnno(User user) {
        roleService.getRolesByUsername(user.getMetadata().getName())
            .collectList()
            .map(JsonUtils::objectToJson)
            .doOnNext(roleNamesJson -> {
                var annotations = Optional.ofNullable(user.getMetadata().getAnnotations())
                    .orElseGet(HashMap::new);
                user.getMetadata().setAnnotations(annotations);
                annotations.put(User.ROLE_NAMES_ANNO, roleNamesJson);
            })
            .block(Duration.ofMinutes(1));
    }

    private void updatePermalink(User user) {
        var name = user.getMetadata().getName();
        if (AnonymousUserConst.isAnonymousUser(name)) {
            // anonymous user is not allowed to have permalink
            return;
        }
        var status = Optional.ofNullable(user.getStatus())
            .orElseGet(User.UserStatus::new);
        user.setStatus(status);
        status.setPermalink(getUserPermalink(user));
    }

    private String getUserPermalink(User user) {
        return UriComponentsBuilder.fromUri(externalUrlSupplier.get())
            .pathSegment("authors", user.getMetadata().getName())
            .toUriString();
    }

    void deleteUserConnections(String username) {
        var userConnections = listConnectionsByUsername(username);
        if (CollectionUtils.isEmpty(userConnections)) {
            return;
        }
        userConnections.forEach(client::delete);
        throw new RequeueException(new Result(true, null), "User connections are not deleted yet");
    }

    List<UserConnection> listConnectionsByUsername(String username) {
        var listOptions = ListOptions.builder()
            .andQuery(equal("spec.username", username))
            .build();
        return client.listAll(UserConnection.class, listOptions, defaultSort());
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new User())
            .build();
    }

}
