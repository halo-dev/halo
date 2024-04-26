package run.halo.app.content.comment;

import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.isNull;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.security.authorization.AuthorityUtils;

/**
 * A default implementation of {@link ReplyService}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {

    private final ReactiveExtensionClient client;
    private final UserService userService;
    private final RoleService roleService;
    private final CounterService counterService;

    @Override
    public Mono<Reply> create(String commentName, Reply reply) {
        return client.get(Comment.class, commentName)
            .map(comment -> {
                // Boolean allowNotification = reply.getSpec().getAllowNotification();
                // TODO send notification if allowNotification is true
                reply.getSpec().setCommentName(commentName);
                if (reply.getSpec().getTop() == null) {
                    reply.getSpec().setTop(false);
                }
                if (reply.getSpec().getPriority() == null) {
                    reply.getSpec().setPriority(0);
                }
                if (reply.getSpec().getCreationTime() == null) {
                    reply.getSpec().setCreationTime(Instant.now());
                }
                if (reply.getSpec().getApproved() == null) {
                    reply.getSpec().setApproved(false);
                }
                if (BooleanUtils.isTrue(reply.getSpec().getApproved())
                    && reply.getSpec().getApprovedTime() == null) {
                    reply.getSpec().setApprovedTime(Instant.now());
                }
                return reply;
            })
            .flatMap(replyToUse -> {
                if (replyToUse.getSpec().getOwner() != null) {
                    return Mono.just(replyToUse);
                }
                // populate owner from current user
                return fetchCurrentUser()
                    .flatMap(user ->
                        ReactiveSecurityContextHolder.getContext()
                            .flatMap(securityContext -> {
                                var authentication = securityContext.getAuthentication();
                                var roles = AuthorityUtils.authoritiesToRoles(
                                    authentication.getAuthorities());
                                return roleService.contains(roles,
                                        Set.of(AuthorityUtils.COMMENT_MANAGEMENT_ROLE_NAME))
                                    .doOnNext(result -> {
                                        if (result) {
                                            reply.getSpec().setApproved(true);
                                            reply.getSpec().setApprovedTime(Instant.now());
                                        }
                                        replyToUse.getSpec().setOwner(toCommentOwner(user));
                                    })
                                    .thenReturn(replyToUse);
                            })
                    )
                    .switchIfEmpty(
                        Mono.error(new IllegalArgumentException("Reply owner must not be null.")));
            })
            .flatMap(client::create);
    }

    @Override
    public Mono<ListResult<ListedReply>> list(ReplyQuery query) {
        return client.listBy(Reply.class, query.toListOptions(), query.toPageRequest())
            .flatMap(list -> Flux.fromStream(list.get()
                    .map(this::toListedReply))
                .concatMap(Function.identity())
                .collectList()
                .map(listedReplies -> new ListResult<>(list.getPage(), list.getSize(),
                    list.getTotal(), listedReplies))
            );
    }

    @Override
    public Mono<Void> removeAllByComment(String commentName) {
        Assert.notNull(commentName, "The commentName must not be null.");
        return cleanupComments(commentName, 200);
    }

    private Mono<Void> cleanupComments(String commentName, int batchSize) {
        // ascending order by creation time and name
        final var pageRequest = PageRequestImpl.of(1, batchSize,
            Sort.by("metadata.creationTimestamp", "metadata.name"));
        // forever loop first page until no more to delete
        return listRepliesByComment(commentName, pageRequest)
            .flatMap(page -> Flux.fromIterable(page.getItems())
                .flatMap(this::deleteWithRetry)
                .then(page.hasNext() ? cleanupComments(commentName, batchSize) : Mono.empty())
            );
    }

    private Mono<Reply> deleteWithRetry(Reply item) {
        return client.delete(item)
            .onErrorResume(OptimisticLockingFailureException.class,
                e -> attemptToDelete(item.getMetadata().getName()));
    }

    private Mono<Reply> attemptToDelete(String name) {
        return Mono.defer(() -> client.fetch(Reply.class, name)
                .flatMap(client::delete)
            )
            .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance));
    }

    Mono<ListResult<Reply>> listRepliesByComment(String commentName, PageRequest pageRequest) {
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(
            and(equal("spec.commentName", commentName),
                isNull("metadata.deletionTimestamp"))
        ));
        return client.listBy(Reply.class, listOptions, pageRequest);
    }

    private Mono<ListedReply> toListedReply(Reply reply) {
        ListedReply.ListedReplyBuilder builder = ListedReply.builder()
            .reply(reply);
        return getOwnerInfo(reply)
            .map(ownerInfo -> {
                builder.owner(ownerInfo);
                return builder;
            })
            .map(ListedReply.ListedReplyBuilder::build)
            .flatMap(listedReply -> fetchStats(reply)
                .doOnNext(listedReply::setStats)
                .thenReturn(listedReply));
    }

    Mono<CommentStats> fetchStats(Reply reply) {
        Assert.notNull(reply, "The reply must not be null.");
        String name = reply.getMetadata().getName();
        return counterService.getByName(MeterUtils.nameOf(Reply.class, name))
            .map(counter -> CommentStats.builder()
                .upvote(counter.getUpvote())
                .build()
            )
            .defaultIfEmpty(CommentStats.empty());
    }

    private Mono<OwnerInfo> getOwnerInfo(Reply reply) {
        Comment.CommentOwner owner = reply.getSpec().getOwner();
        if (User.KIND.equals(owner.getKind())) {
            return userService.getUserOrGhost(owner.getName())
                .map(OwnerInfo::from);
        }
        if (Comment.CommentOwner.KIND_EMAIL.equals(owner.getKind())) {
            return Mono.just(OwnerInfo.from(owner));
        }
        throw new IllegalStateException(
            "Unsupported owner kind: " + owner.getKind());
    }

    Predicate<Reply> getReplyPredicate(ReplyQuery query) {
        Predicate<Reply> predicate = reply -> true;
        if (query.getCommentName() != null) {
            predicate = predicate.and(
                reply -> query.getCommentName().equals(reply.getSpec().getCommentName()));
        }

        Predicate<Extension> labelAndFieldSelectorPredicate =
            labelAndFieldSelectorToPredicate(query.getLabelSelector(),
                query.getFieldSelector());
        return predicate.and(labelAndFieldSelectorPredicate);
    }

    private Comment.CommentOwner toCommentOwner(User user) {
        Comment.CommentOwner owner = new Comment.CommentOwner();
        owner.setKind(User.KIND);
        owner.setName(user.getMetadata().getName());
        owner.setDisplayName(user.getSpec().getDisplayName());
        return owner;
    }

    private Mono<User> fetchCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
            .map(securityContext -> securityContext.getAuthentication().getName())
            .flatMap(username -> client.fetch(User.class, username));
    }
}
