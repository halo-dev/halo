package run.halo.app.theme.finders.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.comment.OwnerInfo;
import run.halo.app.core.extension.Comment;
import run.halo.app.core.extension.Reply;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.theme.finders.CommentFinder;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.vo.CommentVo;
import run.halo.app.theme.finders.vo.ReplyVo;

/**
 * A default implementation of {@link CommentFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("commentFinder")
public class CommentFinderImpl implements CommentFinder {

    private final ReactiveExtensionClient client;

    public CommentFinderImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public CommentVo getByName(String name) {
        return client.fetch(Comment.class, name)
            .flatMap(this::toCommentVo)
            .block();
    }

    @Override
    public ListResult<CommentVo> list(Ref ref, Integer page, Integer size) {
        return client.list(Comment.class, fixedPredicate(ref),
                defaultComparator(),
                pageNullSafe(page), sizeNullSafe(size))
            .flatMap(list -> Flux.fromStream(list.get().map(this::toCommentVo))
                .flatMap(Function.identity())
                .collectList()
                .map(commentVos -> new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    commentVos)
                )
            )
            .block();
    }

    @Override
    public ListResult<ReplyVo> listReply(String commentName, Integer page, Integer size) {
        Comparator<Reply> comparator =
            Comparator.comparing(reply -> reply.getMetadata().getCreationTimestamp());
        return client.list(Reply.class,
                reply -> reply.getSpec().getCommentName().equals(commentName)
                    && Objects.equals(false, reply.getSpec().getHidden())
                    && Objects.equals(true, reply.getSpec().getApproved()),
                comparator.reversed(), pageNullSafe(page), sizeNullSafe(size))
            .flatMap(list -> Flux.fromStream(list.get().map(this::toReplyVo))
                .flatMap(Function.identity())
                .collectList()
                .map(replyVos -> new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    replyVos))
            )
            .block();
    }

    private Mono<CommentVo> toCommentVo(Comment comment) {
        Comment.CommentOwner owner = comment.getSpec().getOwner();
        return Mono.just(CommentVo.from(comment))
            .flatMap(commentVo -> getOwnerInfo(owner)
                .map(commentVo::withOwner)
            );
    }

    private Mono<ReplyVo> toReplyVo(Reply reply) {
        return Mono.just(ReplyVo.from(reply))
            .flatMap(replyVo -> getOwnerInfo(reply.getSpec().getOwner())
                .map(replyVo::withOwner)
            );
    }

    private Mono<OwnerInfo> getOwnerInfo(Comment.CommentOwner owner) {
        if (Comment.CommentOwner.KIND_EMAIL.equals(owner.getKind())) {
            return Mono.just(OwnerInfo.from(owner));
        }
        return client.fetch(User.class, owner.getName())
            .map(OwnerInfo::from)
            .switchIfEmpty(Mono.just(OwnerInfo.ghostUser()));
    }

    private Predicate<Comment> fixedPredicate(Ref ref) {
        Assert.notNull(ref, "Comment subject reference must not be null");
        return comment -> comment.getSpec().getSubjectRef().equals(ref)
            && Objects.equals(false, comment.getSpec().getHidden())
            && Objects.equals(true, comment.getSpec().getApproved());
    }

    static Comparator<Comment> defaultComparator() {
        Function<Comment, Boolean> top =
            comment -> Objects.requireNonNullElse(comment.getSpec().getTop(), false);
        Function<Comment, Integer> priority =
            comment -> Objects.requireNonNullElse(comment.getSpec().getPriority(), 0);
        Function<Comment, Instant> creationTimestamp =
            comment -> comment.getMetadata().getCreationTimestamp();
        Function<Comment, String> name =
            comment -> comment.getMetadata().getName();
        return Comparator.comparing(top)
            .thenComparing(priority)
            .thenComparing(creationTimestamp)
            .thenComparing(name)
            .reversed();
    }

    int pageNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 1);
    }

    int sizeNullSafe(Integer size) {
        return ObjectUtils.defaultIfNull(size, 10);
    }
}
