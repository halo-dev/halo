package run.halo.app.content.comment;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.isNull;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.counter.CounterService;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.exception.RequestRestrictedException;

/**
 * A default implementation of {@link ReplyService}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Service
public class ReplyServiceImpl extends AbstractCommentService implements ReplyService {

    private final Supplier<RequestRestrictedException> requestRestrictedExceptionSupplier =
        () -> new RequestRestrictedException("problemDetail.comment.waitingForApproval");

    public ReplyServiceImpl(RoleService roleService, ReactiveExtensionClient client,
        UserService userService, CounterService counterService) {
        super(roleService, client, userService, counterService);
    }

    @Override
    public Mono<Reply> create(String commentName, Reply reply) {
        return client.get(Comment.class, commentName)
            .flatMap(this::approveComment)
            .filter(comment -> isTrue(comment.getSpec().getApproved()))
            .switchIfEmpty(Mono.error(requestRestrictedExceptionSupplier))
            .flatMap(comment -> prepareReply(commentName, reply))
            .flatMap(this::doCreateReply);
    }

    private Mono<Reply> doCreateReply(Reply prepared) {
        var quotedReply = prepared.getSpec().getQuoteReply();
        if (StringUtils.isBlank(quotedReply)) {
            return client.create(prepared);
        }
        return approveReply(quotedReply)
            .filter(reply -> isTrue(reply.getSpec().getApproved()))
            .switchIfEmpty(Mono.error(requestRestrictedExceptionSupplier))
            .flatMap(approvedQuoteReply -> client.create(prepared));
    }

    private Mono<Comment> approveComment(Comment comment) {
        return hasCommentManagePermission()
            .flatMap(hasPermission -> {
                if (hasPermission) {
                    return doApproveComment(comment);
                }
                return Mono.just(comment);
            });
    }

    private Mono<Comment> doApproveComment(Comment comment) {
        UnaryOperator<Comment> updateFunc = commentToUpdate -> {
            commentToUpdate.getSpec().setApproved(true);
            commentToUpdate.getSpec().setApprovedTime(Instant.now());
            return commentToUpdate;
        };
        return client.update(updateFunc.apply(comment))
            .onErrorResume(OptimisticLockingFailureException.class,
                e -> updateCommentWithRetry(comment.getMetadata().getName(), updateFunc));
    }

    private Mono<Reply> approveReply(String replyName) {
        return hasCommentManagePermission()
            .flatMap(hasPermission -> {
                if (hasPermission) {
                    return doApproveReply(replyName);
                }
                return client.get(Reply.class, replyName);
            });
    }

    private Mono<Reply> doApproveReply(String replyName) {
        return Mono.defer(() -> client.get(Reply.class, replyName)
                .flatMap(reply -> {
                    reply.getSpec().setApproved(true);
                    reply.getSpec().setApprovedTime(Instant.now());
                    return client.update(reply);
                })
            )
            .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance));
    }

    private Mono<Comment> updateCommentWithRetry(String name, UnaryOperator<Comment> updateFunc) {
        return Mono.defer(() -> client.get(Comment.class, name)
                .map(updateFunc)
                .flatMap(client::update)
            )
            .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance));
    }

    private Mono<Reply> prepareReply(String commentName, Reply reply) {
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
        if (isTrue(reply.getSpec().getApproved())
            && reply.getSpec().getApprovedTime() == null) {
            reply.getSpec().setApprovedTime(Instant.now());
        }

        var steps = new ArrayList<Publisher<?>>();
        var approveItMono = hasCommentManagePermission()
            .filter(Boolean::booleanValue)
            .doOnNext(hasPermission -> {
                reply.getSpec().setApproved(true);
                reply.getSpec().setApprovedTime(Instant.now());
            });
        steps.add(approveItMono);

        var populateOwnerMono = fetchCurrentUser()
            .switchIfEmpty(
                Mono.error(new IllegalArgumentException("Reply owner must not be null.")))
            .doOnNext(user -> reply.getSpec().setOwner(toCommentOwner(user)));
        if (reply.getSpec().getOwner() == null) {
            steps.add(populateOwnerMono);
        }
        return Mono.when(steps).thenReturn(reply);
    }

    @Override
    public Mono<ListResult<ListedReply>> list(ReplyQuery query) {
        return client.listBy(Reply.class, query.toListOptions(), query.toPageRequest())
            .flatMap(list -> Flux.fromStream(list.get()
                    .map(this::toListedReply))
                .flatMapSequential(Function.identity())
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
        return getOwnerInfo(reply.getSpec().getOwner())
            .map(ownerInfo -> {
                builder.owner(ownerInfo);
                return builder;
            })
            .map(ListedReply.ListedReplyBuilder::build)
            .flatMap(listedReply -> fetchReplyStats(reply.getMetadata().getName())
                .doOnNext(listedReply::setStats)
                .thenReturn(listedReply));
    }
}
