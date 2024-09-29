package run.halo.app.content.comment;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.core.counter.CounterService;
import run.halo.app.core.counter.MeterUtils;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.security.authorization.AuthorityUtils;

@RequiredArgsConstructor
public abstract class AbstractCommentService {
    protected final RoleService roleService;
    protected final ReactiveExtensionClient client;
    protected final UserService userService;
    protected final CounterService counterService;

    protected Mono<User> fetchCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
            .map(securityContext -> securityContext.getAuthentication().getName())
            .flatMap(username -> client.fetch(User.class, username));
    }

    Mono<Boolean> hasCommentManagePermission() {
        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                var authentication = securityContext.getAuthentication();
                var roles = AuthorityUtils.authoritiesToRoles(authentication.getAuthorities());
                return roleService.contains(roles,
                    Set.of(AuthorityUtils.COMMENT_MANAGEMENT_ROLE_NAME));
            });
    }

    protected Comment.CommentOwner toCommentOwner(User user) {
        Comment.CommentOwner owner = new Comment.CommentOwner();
        owner.setKind(User.KIND);
        owner.setName(user.getMetadata().getName());
        owner.setDisplayName(user.getSpec().getDisplayName());
        return owner;
    }

    protected Mono<OwnerInfo> getOwnerInfo(Comment.CommentOwner owner) {
        if (User.KIND.equals(owner.getKind())) {
            return userService.getUserOrGhost(owner.getName())
                .map(OwnerInfo::from);
        }
        if (Comment.CommentOwner.KIND_EMAIL.equals(owner.getKind())) {
            return Mono.just(OwnerInfo.from(owner));
        }
        return Mono.error(new IllegalStateException("Unsupported owner kind: " + owner.getKind()));
    }

    protected Mono<CommentStats> fetchCommentStats(String commentName) {
        return this.fetchStats(MeterUtils.nameOf(Comment.class, commentName));
    }

    protected Mono<CommentStats> fetchReplyStats(String replyName) {
        return this.fetchStats(MeterUtils.nameOf(Reply.class, replyName));
    }

    private Mono<CommentStats> fetchStats(String meterName) {
        Assert.notNull(meterName, "The reply must not be null.");
        return counterService.getByName(meterName)
            .map(counter -> CommentStats.builder()
                .upvote(counter.getUpvote())
                .build()
            )
            .switchIfEmpty(Mono.fromSupplier(CommentStats::empty));
    }
}
