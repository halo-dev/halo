package run.halo.app.theme.finders.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.springframework.util.Assert;
import run.halo.app.core.extension.Comment;
import run.halo.app.core.extension.Reply;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
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
            .map(CommentVo::from)
            .block();
    }

    @Override
    public ListResult<CommentVo> list(Comment.CommentSubjectRef ref, int page, int size) {
        return client.list(Comment.class, fixedPredicate(ref),
                defaultComparator(),
                Math.max(page - 1, 0), size)
            .map(list -> {
                List<CommentVo> commentVos = list.get().map(CommentVo::from).toList();
                return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    commentVos);
            })
            .block();
    }

    @Override
    public ListResult<ReplyVo> listReply(String commentName, int page, int size) {
        Comparator<Reply> comparator =
            Comparator.comparing(reply -> reply.getMetadata().getCreationTimestamp());
        return client.list(Reply.class,
                reply -> reply.getSpec().getCommentName().equals(commentName)
                    && Objects.equals(false, reply.getSpec().getHidden())
                    && Objects.equals(true, reply.getSpec().getApproved()),
                comparator.reversed(), page, size)
            .map(list -> {
                List<ReplyVo> replyVos = list.get().map(ReplyVo::from).toList();
                return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    replyVos);
            })
            .block();
    }

    private Predicate<Comment> fixedPredicate(Comment.CommentSubjectRef ref) {
        Assert.notNull(ref, "Comment subject reference must not be null");
        return comment -> ref.equals(comment.getSpec().getSubjectRef())
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
}
