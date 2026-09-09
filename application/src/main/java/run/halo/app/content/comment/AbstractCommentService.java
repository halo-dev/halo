package run.halo.app.content.comment;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.counter.CounterService;
import run.halo.app.core.counter.MeterUtils;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.security.authorization.AuthorityUtils;

@RequiredArgsConstructor
public abstract class AbstractCommentService {
    protected final RoleService roleService;
    protected final ReactiveExtensionClient client;
    protected final UserService userService;
    protected final CounterService counterService;
    private final Safelist safelist = Safelist.relaxed()
            // Allow <s> tag, which is used for strikethrough
            .addTags("s")
            // Allow <code> tag's class attribute, for syntax highlighting
            .addAttributes("code", "class")
            // Allow <a> tag's target attribute
            .addAttributes("a", "target")
            .preserveRelativeLinks(true);

    protected Mono<User> fetchCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getName())
                .flatMap(username -> client.fetch(User.class, username));
    }

    Mono<Boolean> hasCommentManagePermission() {
        return ReactiveSecurityContextHolder.getContext().flatMap(securityContext -> {
            var authentication = securityContext.getAuthentication();
            var roles = AuthorityUtils.authoritiesToRoles(authentication.getAuthorities());
            return roleService.contains(roles, Set.of(AuthorityUtils.COMMENT_MANAGEMENT_ROLE_NAME));
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
            return userService.getUserOrGhost(owner.getName()).map(OwnerInfo::from);
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
        return counterService
                .getByName(meterName)
                .map(counter ->
                        CommentStats.builder().upvote(counter.getUpvote()).build())
                .switchIfEmpty(Mono.fromSupplier(CommentStats::empty));
    }

    /**
     * Check if the given html is a safe HTML.
     *
     * @param html html content
     * @return true if the html is safe, false otherwise
     */
    protected boolean isSafeHtml(String html) {
        return Jsoup.isValid(html, safelist);
    }

    protected void updateContent(
            Comment.BaseCommentSpec spec, MetadataOperator metadata, CommentContentRequest request) {
        if (!StringUtils.hasText(request.raw())
                || !StringUtils.hasText(request.content())
                || request.version() == null
                || !isSafeHtml(request.content())) {
            throw new ServerWebInputException("A version and non-empty, safe comment body are required.");
        }
        var body = Jsoup.parseBodyFragment(request.content()).body();
        if (body.text().isBlank()
                && body.select("img[src]").stream().noneMatch(image -> StringUtils.hasText(image.attr("src")))) {
            throw new ServerWebInputException("The comment body must not be empty.");
        }
        if (metadata.getDeletionTimestamp() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot edit a deleted comment or reply.");
        }
        if (!request.version().equals(metadata.getVersion())) {
            throw new OptimisticLockingFailureException("The comment or reply has changed. Reload before editing.");
        }
        spec.setRaw(request.raw());
        spec.setContent(request.content());
    }
}
