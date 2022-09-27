package run.halo.app.content.comment;

import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import java.time.Instant;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Comment;
import run.halo.app.core.extension.Reply;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.exception.AccessDeniedException;

/**
 * A default implementation of {@link ReplyService}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Service
public class ReplyServiceImpl implements ReplyService {

    private final ReactiveExtensionClient client;
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    public ReplyServiceImpl(ReactiveExtensionClient client,
        SystemConfigurableEnvironmentFetcher environmentFetcher) {
        this.client = client;
        this.environmentFetcher = environmentFetcher;
    }

    @Override
    public Mono<Reply> create(String commentName, Reply reply) {
        return client.fetch(Comment.class, commentName)
            .flatMap(comment -> {
                // Boolean allowNotification = reply.getSpec().getAllowNotification();
                // TODO send notification if allowNotification is true
                reply.getSpec().setCommentName(commentName);
                if (reply.getSpec().getTop() == null) {
                    reply.getSpec().setTop(false);
                }
                if (reply.getSpec().getPriority() == null) {
                    reply.getSpec().setPriority(0);
                }
                return environmentFetcher.fetchComment()
                    .map(commentSetting -> {
                        if (Boolean.FALSE.equals(commentSetting.getEnable())) {
                            throw new AccessDeniedException(
                                "The comment function has been turned off.");
                        }
                        if (checkReplyOwner(reply, commentSetting.getSystemUserOnly())) {
                            throw new AccessDeniedException("Allow system user reply only.");
                        }
                        reply.getSpec().setApproved(
                            Boolean.FALSE.equals(commentSetting.getRequireReviewForNew()));
                        reply.getSpec().setHidden(!reply.getSpec().getApproved());
                        return reply;
                    });
            })
            .flatMap(replyToUse -> {
                if (replyToUse.getSpec().getOwner() != null) {
                    return Mono.just(replyToUse);
                }
                // populate owner from current user
                return fetchCurrentUser()
                    .map(user -> {
                        replyToUse.getSpec().setOwner(toCommentOwner(user));
                        return replyToUse;
                    })
                    .switchIfEmpty(
                        Mono.error(new IllegalStateException("Reply owner must not be null.")));
            })
            .flatMap(client::create)
            .switchIfEmpty(Mono.error(
                new IllegalArgumentException(
                    String.format("Comment not found for name [%s].", commentName)))
            );
    }

    private boolean checkReplyOwner(Reply reply, Boolean onlySystemUser) {
        Comment.CommentOwner owner = reply.getSpec().getOwner();
        if (Boolean.TRUE.equals(onlySystemUser)) {
            return owner != null && Comment.CommentOwner.KIND_EMAIL.equals(owner.getKind());
        }
        return false;
    }

    @Override
    public Mono<ListResult<ListedReply>> list(ReplyQuery query) {
        return client.list(Reply.class, getReplyPredicate(query), defaultComparator(),
                query.getPage(), query.getSize())
            .flatMap(list -> Flux.fromStream(list.get()
                    .map(this::toListedReply))
                .flatMap(Function.identity())
                .collectList()
                .map(listedReplies -> new ListResult<>(list.getPage(), list.getSize(),
                    list.getTotal(), listedReplies))
            );
    }

    private Mono<ListedReply> toListedReply(Reply reply) {
        ListedReply.ListedReplyBuilder builder = ListedReply.builder()
            .reply(reply);
        return getOwnerInfo(reply)
            .map(ownerInfo -> {
                builder.owner(ownerInfo);
                return builder;
            })
            .map(ListedReply.ListedReplyBuilder::build);
    }

    private Mono<OwnerInfo> getOwnerInfo(Reply reply) {
        Comment.CommentOwner owner = reply.getSpec().getOwner();
        if (User.KIND.equals(owner.getKind())) {
            return client.fetch(User.class, owner.getName())
                .map(OwnerInfo::from)
                .switchIfEmpty(Mono.just(OwnerInfo.ghostUser()));
        }
        if (Comment.CommentOwner.KIND_EMAIL.equals(owner.getKind())) {
            return Mono.just(OwnerInfo.from(owner));
        }
        throw new IllegalStateException(
            "Unsupported owner kind: " + owner.getKind());
    }

    Comparator<Reply> defaultComparator() {
        Function<Reply, Instant> createTime = reply -> reply.getMetadata().getCreationTimestamp();
        return Comparator.comparing(createTime)
            .thenComparing(reply -> reply.getMetadata().getName());
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
