package run.halo.app.theme.finders.impl;

import java.security.Principal;
import java.time.Instant;
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
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.comment.OwnerInfo;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.infra.AnonymousUserConst;
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
@RequiredArgsConstructor
public class CommentFinderImpl implements CommentFinder {

    private final ReactiveExtensionClient client;
    private final UserService userService;

    @Override
    public Mono<CommentVo> getByName(String name) {
        return client.fetch(Comment.class, name)
            .flatMap(this::toCommentVo);
    }

    @Override
    public Mono<ListResult<CommentVo>> list(Ref ref, Integer page, Integer size) {
        return fixedCommentPredicate(ref)
            .flatMap(fixedPredicate ->
                client.list(Comment.class, fixedPredicate,
                        defaultComparator(),
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
        Comparator<Reply> comparator =
            Comparator.comparing(reply -> reply.getMetadata().getCreationTimestamp());
        return fixedReplyPredicate(commentName)
            .flatMap(fixedPredicate ->
                client.list(Reply.class, fixedPredicate,
                        comparator.reversed(), pageNullSafe(page), sizeNullSafe(size))
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
            .flatMap(commentVo -> getOwnerInfo(owner)
                .map(commentVo::withOwner)
                .defaultIfEmpty(commentVo)
            );
    }

    private Mono<ReplyVo> toReplyVo(Reply reply) {
        return Mono.just(ReplyVo.from(reply))
            .flatMap(replyVo -> getOwnerInfo(reply.getSpec().getOwner())
                .map(replyVo::withOwner)
                .defaultIfEmpty(replyVo)
            );
    }

    private Mono<OwnerInfo> getOwnerInfo(Comment.CommentOwner owner) {
        if (Comment.CommentOwner.KIND_EMAIL.equals(owner.getKind())) {
            return Mono.just(OwnerInfo.from(owner));
        }
        return userService.userOrGhost(owner.getName())
            .map(OwnerInfo::from)
            .defaultIfEmpty(OwnerInfo.ghostUser());
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
