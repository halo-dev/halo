package run.halo.app.content.comment;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Comment;
import run.halo.app.core.extension.Reply;
import run.halo.app.core.extension.User;
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
                return environmentFetcher.fetchComment()
                    .map(commentSetting -> {
                        if (Boolean.FALSE.equals(commentSetting.getEnable())) {
                            throw new AccessDeniedException(
                                "The comment function has been turned off.");
                        }
                        reply.getSpec().setApproved(
                            Boolean.FALSE.equals(commentSetting.getRequireReviewForNew()));
                        reply.getSpec().setHidden(reply.getSpec().getApproved());
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
