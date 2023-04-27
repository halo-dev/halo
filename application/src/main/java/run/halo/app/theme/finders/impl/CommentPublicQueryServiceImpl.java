package run.halo.app.theme.finders.impl;


import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.comparator.Comparators;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.comment.OwnerInfo;
import run.halo.app.content.comment.ReplyService;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.theme.finders.CommentPublicQueryService;
import run.halo.app.theme.finders.vo.CommentStatsVo;
import run.halo.app.theme.finders.vo.CommentVo;
import run.halo.app.theme.finders.vo.ExtensionVoOperator;
import run.halo.app.theme.finders.vo.ReplyVo;

/**
 * comment public query service implementation.
 *
 * @author LIlGG
 */
@Component
@RequiredArgsConstructor
public class CommentPublicQueryServiceImpl implements CommentPublicQueryService {

    private final ReactiveExtensionClient client;
    private final UserService userService;
    private final CounterService counterService;

    @Override
    public Mono<CommentVo> getByName(String name) {
        return client.fetch(Comment.class, name)
            .flatMap(this::toCommentVo);
    }

    @Override
    public Mono<ListResult<CommentVo>> list(Ref ref, Integer page, Integer size) {
        return list(ref, page, size, defaultComparator());
    }

    @Override
    public Mono<ListResult<CommentVo>> list(Ref ref, Integer page, Integer size,
        Comparator<Comment> comparator) {
        final Comparator<Comment> commentComparator =
            Objects.isNull(comparator) ? defaultComparator()
                : comparator.thenComparing(defaultComparator());
        return fixedCommentPredicate(ref)
            .flatMap(fixedPredicate ->
                client.list(Comment.class, fixedPredicate,
                        commentComparator,
                        pageNullSafe(page), sizeNullSafe(size))
                    .flatMap(list -> Flux.fromStream(list.get().map(this::toCommentVo))
                        .concatMap(Function.identity())
                        .collectList()
                        .map(commentVos -> new ListResult<>(list.getPage(), list.getSize(),
                            list.getTotal(),
                            commentVos)
                        )
                    )
                    .defaultIfEmpty(new ListResult<>(page, size, 0L, List.of()))
            );
    }

    @Override
    public Mono<ListResult<ReplyVo>> listReply(String commentName, Integer page, Integer size) {
        return listReply(commentName, page, size, ReplyService.creationTimeAscComparator());
    }

    @Override
    public Mono<ListResult<ReplyVo>> listReply(String commentName, Integer page, Integer size,
        Comparator<Reply> comparator) {
        return fixedReplyPredicate(commentName)
            .flatMap(fixedPredicate ->
                client.list(Reply.class, fixedPredicate,
                        comparator,
                        pageNullSafe(page), sizeNullSafe(size))
                    .flatMap(list -> Flux.fromStream(list.get().map(this::toReplyVo))
                        .concatMap(Function.identity())
                        .collectList()
                        .map(replyVos -> new ListResult<>(list.getPage(), list.getSize(),
                            list.getTotal(),
                            replyVos))
                    )
                    .defaultIfEmpty(new ListResult<>(page, size, 0L, List.of()))
            );
    }

    private Mono<CommentVo> toCommentVo(Comment comment) {
        Comment.CommentOwner owner = comment.getSpec().getOwner();
        return Mono.just(CommentVo.from(comment))
            .flatMap(commentVo -> populateStats(Comment.class, commentVo)
                .doOnNext(commentVo::setStats)
                .thenReturn(commentVo))
            .flatMap(commentVo -> getOwnerInfo(owner)
                .doOnNext(commentVo::setOwner)
                .thenReturn(commentVo)
            );
    }

    private <E extends AbstractExtension, T extends ExtensionVoOperator> Mono<CommentStatsVo>
        populateStats(Class<E> clazz, T vo) {
        return counterService.getByName(MeterUtils.nameOf(clazz, vo.getMetadata()
                .getName()))
            .map(counter -> CommentStatsVo.builder()
                .upvote(counter.getUpvote())
                .build()
            )
            .defaultIfEmpty(CommentStatsVo.empty());
    }

    private Mono<ReplyVo> toReplyVo(Reply reply) {
        return Mono.just(ReplyVo.from(reply))
            .flatMap(replyVo -> populateStats(Reply.class, replyVo)
                .doOnNext(replyVo::setStats)
                .thenReturn(replyVo))
            .flatMap(replyVo -> getOwnerInfo(reply.getSpec().getOwner())
                .doOnNext(replyVo::setOwner)
                .thenReturn(replyVo)
            );
    }

    private Mono<OwnerInfo> getOwnerInfo(Comment.CommentOwner owner) {
        if (Comment.CommentOwner.KIND_EMAIL.equals(owner.getKind())) {
            return Mono.just(OwnerInfo.from(owner));
        }
        return userService.getUserOrGhost(owner.getName())
            .map(OwnerInfo::from);
    }

    private Mono<Predicate<Comment>> fixedCommentPredicate(Ref ref) {
        Assert.notNull(ref, "Comment subject reference must not be null");
        // Ref must be equal to the comment subject
        Predicate<Comment> refPredicate = comment -> comment.getSpec().getSubjectRef().equals(ref)
            && comment.getMetadata().getDeletionTimestamp() == null;

        // is approved and not hidden
        Predicate<Comment> approvedPredicate =
            comment -> BooleanUtils.isFalse(comment.getSpec().getHidden())
                && BooleanUtils.isTrue(comment.getSpec().getApproved());
        return getCurrentUserWithoutAnonymous()
            .map(username -> {
                Predicate<Comment> isOwner = comment -> {
                    Comment.CommentOwner owner = comment.getSpec().getOwner();
                    return owner != null && StringUtils.equals(username, owner.getName());
                };
                return approvedPredicate.or(isOwner);
            })
            .defaultIfEmpty(approvedPredicate)
            .map(refPredicate::and);
    }

    private Mono<Predicate<Reply>> fixedReplyPredicate(String commentName) {
        Assert.notNull(commentName, "The commentName must not be null");
        // The comment name must be equal to the comment name of the reply
        Predicate<Reply> commentNamePredicate =
            reply -> reply.getSpec().getCommentName().equals(commentName)
                && reply.getMetadata().getDeletionTimestamp() == null;

        // is approved and not hidden
        Predicate<Reply> approvedPredicate =
            reply -> BooleanUtils.isFalse(reply.getSpec().getHidden())
                && BooleanUtils.isTrue(reply.getSpec().getApproved());
        return getCurrentUserWithoutAnonymous()
            .map(username -> {
                Predicate<Reply> isOwner = reply -> {
                    Comment.CommentOwner owner = reply.getSpec().getOwner();
                    return owner != null && StringUtils.equals(username, owner.getName());
                };
                return approvedPredicate.or(isOwner);
            })
            .defaultIfEmpty(approvedPredicate)
            .map(commentNamePredicate::and);
    }

    Mono<String> getCurrentUserWithoutAnonymous() {
        return ReactiveSecurityContextHolder.getContext()
            .mapNotNull(SecurityContext::getAuthentication)
            .map(Principal::getName)
            .filter(username -> !AnonymousUserConst.PRINCIPAL.equals(username));
    }

    static Comparator<Comment> defaultComparator() {
        return new CommentComparator();
    }

    static class CommentComparator implements Comparator<Comment> {
        @Override
        public int compare(Comment c1, Comment c2) {
            boolean c1Top = BooleanUtils.isTrue(c1.getSpec().getTop());
            boolean c2Top = BooleanUtils.isTrue(c2.getSpec().getTop());
            if (c1Top == c2Top) {
                int c1Priority = ObjectUtils.defaultIfNull(c1.getSpec().getPriority(), 0);
                int c2Priority = ObjectUtils.defaultIfNull(c2.getSpec().getPriority(), 0);
                if (c1Top) {
                    // 都置顶
                    return Integer.compare(c1Priority, c2Priority);
                }

                // 两个评论不置顶根据 creationTime 降序排列
                return Comparator.comparing(
                        (Comment comment) -> comment.getSpec().getCreationTime(),
                        Comparators.nullsLow())
                    .thenComparing((Comment comment) -> comment.getMetadata().getName())
                    .compare(c2, c1);
            } else if (c1Top) {
                // 只有 c1 置顶，c1 排前面
                return -1;
            } else {
                // 只有c2置顶, c2排在前面
                return 1;
            }
        }
    }

    int pageNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 1);
    }

    int sizeNullSafe(Integer size) {
        return ObjectUtils.defaultIfNull(size, 10);
    }
}
