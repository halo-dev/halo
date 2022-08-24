package run.halo.app.theme.finders.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Comment;
import run.halo.app.core.extension.Reply;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.CommentFinder;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.SubscriberUtils;
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
    private static final Comparator<Comment> DEFAULT_ORDER = (o1, o2) -> {
        Assert.notNull(o1, "o1 must not be null");
        Assert.notNull(o2, "o2 must not be null");
        if (o1 == o2) {
            return 0;
        }
        if (Objects.equals(true, o1.getSpec().getTop())) {
            if (Objects.equals(true, o2.getSpec().getTop())) {
                return o1.getSpec().getPriority().compareTo(o2.getSpec().getPriority());
            }
            return 1;
        }

        int compare = o1.getMetadata().getCreationTimestamp()
            .compareTo(o2.getMetadata().getCreationTimestamp());
        return compare == 0 ? o1.getMetadata().getName()
            .compareTo(o2.getMetadata().getName()) : compare;
    };

    private final ReactiveExtensionClient client;

    public CommentFinderImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public CommentVo getByName(String name) {
        Mono<CommentVo> mono = client.fetch(Comment.class, name)
            .map(CommentVo::from);
        return SubscriberUtils.subscribe(mono);
    }

    @Override
    public ListResult<CommentVo> list(Comment.CommentSubjectRef ref, int page, int size) {
        Mono<ListResult<CommentVo>> mono =
            client.list(Comment.class, fixedPredicate(ref),
                    DEFAULT_ORDER.reversed(),
                    Math.max(page - 1, 0), size)
                .map(list -> {
                    List<CommentVo> commentVos = list.get().map(CommentVo::from).toList();
                    return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                        commentVos);
                });
        return SubscriberUtils.subscribe(mono);
    }

    @Override
    public ListResult<ReplyVo> listReply(String commentName, int page, int size) {
        Comparator<Reply> comparator =
            Comparator.comparing(reply -> reply.getMetadata().getCreationTimestamp());
        Mono<ListResult<ReplyVo>> mono = client.list(Reply.class,
                reply -> reply.getSpec().getCommentName().equals(commentName)
                    && Objects.equals(false, reply.getSpec().getHidden())
                    && Objects.equals(true, reply.getSpec().getApproved()),
                comparator.reversed(), page, size)
            .map(list -> {
                List<ReplyVo> replyVos = list.get().map(ReplyVo::from).toList();
                return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    replyVos);
            });
        return SubscriberUtils.subscribe(mono);
    }

    private Predicate<Comment> fixedPredicate(Comment.CommentSubjectRef ref) {
        Assert.notNull(ref, "Comment subject reference must not be null");
        return comment -> ref.equals(comment.getSpec().getSubjectRef())
            && Objects.equals(false, comment.getSpec().getHidden())
            && Objects.equals(true, comment.getSpec().getApproved());
    }
}
