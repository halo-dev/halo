package run.halo.app.theme.finders.impl;


import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.isNull;
import static run.halo.app.extension.index.query.QueryFactory.or;

import java.security.Principal;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.comment.OwnerInfo;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.theme.finders.CommentPublicQueryService;
import run.halo.app.theme.finders.vo.CommentStatsVo;
import run.halo.app.theme.finders.vo.CommentVo;
import run.halo.app.theme.finders.vo.CommentWithReplyVo;
import run.halo.app.theme.finders.vo.ExtensionVoOperator;
import run.halo.app.theme.finders.vo.ReplyVo;

/**
 * comment public query service implementation.
 *
 * @author LIlGG
 * @author guqing
 */
@Component
@RequiredArgsConstructor
public class CommentPublicQueryServiceImpl implements CommentPublicQueryService {
    private static final int DEFAULT_SIZE = 10;

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
        return list(ref,
            PageRequestImpl.of(pageNullSafe(page), sizeNullSafe(size), defaultCommentSort()));
    }

    @Override
    public Mono<ListResult<CommentVo>> list(Ref ref, PageRequest pageParam) {
        var pageRequest = Optional.ofNullable(pageParam)
            .map(page -> page.withSort(page.getSort().and(defaultCommentSort())))
            .orElse(PageRequestImpl.ofSize(0));
        return fixedCommentFieldSelector(ref)
            .flatMap(fieldSelector -> {
                var listOptions = new ListOptions();
                listOptions.setFieldSelector(fieldSelector);
                return client.listBy(Comment.class, listOptions, pageRequest)
                    .flatMap(listResult -> Flux.fromStream(listResult.get())
                        .map(this::toCommentVo)
                        .concatMap(Function.identity())
                        .collectList()
                        .map(commentVos -> new ListResult<>(listResult.getPage(),
                            listResult.getSize(),
                            listResult.getTotal(),
                            commentVos)
                        )
                    );
            })
            .defaultIfEmpty(ListResult.emptyResult());
    }

    @Override
    public Mono<ListResult<CommentWithReplyVo>> convertToWithReplyVo(ListResult<CommentVo> comments,
        int replySize) {
        return Flux.fromIterable(comments.getItems())
            .concatMap(commentVo -> {
                var commentName = commentVo.getMetadata().getName();
                return listReply(commentName, 1, replySize)
                    .map(replyList -> CommentWithReplyVo.from(commentVo)
                        .setReplies(replyList)
                    );
            })
            .collectList()
            .map(result -> new ListResult<>(
                comments.getPage(),
                comments.getSize(),
                comments.getTotal(),
                result)
            );
    }

    @Override
    public Mono<ListResult<ReplyVo>> listReply(String commentName, Integer page, Integer size) {
        return listReply(commentName, PageRequestImpl.of(pageNullSafe(page), sizeNullSafe(size),
            defaultReplySort()));
    }

    @Override
    public Mono<ListResult<ReplyVo>> listReply(String commentName, PageRequest pageParam) {
        return fixedReplyFieldSelector(commentName)
            .flatMap(fieldSelector -> {
                var listOptions = new ListOptions();
                listOptions.setFieldSelector(fieldSelector);
                var pageRequest = Optional.ofNullable(pageParam)
                    .map(page -> page.withSort(page.getSort().and(defaultReplySort())))
                    .orElse(PageRequestImpl.ofSize(0));
                return client.listBy(Reply.class, listOptions, pageRequest)
                    .flatMap(list -> Flux.fromStream(list.get().map(this::toReplyVo))
                        .concatMap(Function.identity())
                        .collectList()
                        .map(replyVos -> new ListResult<>(list.getPage(), list.getSize(),
                            list.getTotal(),
                            replyVos))
                    );
            })
            .defaultIfEmpty(ListResult.emptyResult());
    }

    Mono<CommentVo> toCommentVo(Comment comment) {
        Comment.CommentOwner owner = comment.getSpec().getOwner();
        return Mono.just(CommentVo.from(comment))
            .flatMap(commentVo -> populateStats(Comment.class, commentVo)
                .doOnNext(commentVo::setStats)
                .thenReturn(commentVo))
            .flatMap(commentVo -> getOwnerInfo(owner)
                .doOnNext(commentVo::setOwner)
                .thenReturn(commentVo)
            )
            .flatMap(this::filterCommentSensitiveData);
    }

    private Mono<? extends CommentVo> filterCommentSensitiveData(CommentVo commentVo) {
        var owner = commentVo.getOwner();
        commentVo.setOwner(OwnerInfo
            .builder()
            .displayName(owner.getDisplayName())
            .avatar(owner.getAvatar())
            .kind(owner.getKind())
            .build());

        commentVo.getSpec().setIpAddress("");
        var specOwner = commentVo.getSpec().getOwner();
        specOwner.setName("");
        var email = owner.getEmail();
        if (StringUtils.isNotBlank(email)) {
            var emailHash = DigestUtils.md5DigestAsHex(email.getBytes());
            if (specOwner.getAnnotations() == null) {
                specOwner.setAnnotations(new HashMap<>(2));
            }
            specOwner.getAnnotations()
                .put(Comment.CommentOwner.EMAIL_HASH_ANNO, emailHash);
        }
        if (specOwner.getAnnotations() != null) {
            specOwner.getAnnotations().remove("Email");
        }
        return Mono.just(commentVo);
    }

    // @formatter:off
    private <E extends AbstractExtension, T extends ExtensionVoOperator>
        Mono<CommentStatsVo> populateStats(Class<E> clazz, T vo) {
        return counterService.getByName(MeterUtils.nameOf(clazz, vo.getMetadata()
                .getName()))
            .map(counter -> CommentStatsVo.builder()
                .upvote(counter.getUpvote())
                .build()
            )
            .defaultIfEmpty(CommentStatsVo.empty());
    }
    // @formatter:on

    Mono<ReplyVo> toReplyVo(Reply reply) {
        return Mono.just(ReplyVo.from(reply))
            .flatMap(replyVo -> populateStats(Reply.class, replyVo)
                .doOnNext(replyVo::setStats)
                .thenReturn(replyVo))
            .flatMap(replyVo -> getOwnerInfo(reply.getSpec().getOwner())
                .doOnNext(replyVo::setOwner)
                .thenReturn(replyVo)
            )
            .flatMap(this::filterReplySensitiveData);
    }

    private Mono<? extends ReplyVo> filterReplySensitiveData(ReplyVo replyVo) {
        var owner = replyVo.getOwner();
        replyVo.setOwner(OwnerInfo
            .builder()
            .displayName(owner.getDisplayName())
            .avatar(owner.getAvatar())
            .kind(owner.getKind())
            .build());

        replyVo.getSpec().setIpAddress("");
        var specOwner = replyVo.getSpec().getOwner();
        specOwner.setName("");
        var email = owner.getEmail();
        if (StringUtils.isNotBlank(email)) {
            var emailHash = DigestUtils.md5DigestAsHex(email.getBytes());
            if (specOwner.getAnnotations() == null) {
                specOwner.setAnnotations(new HashMap<>(2));
            }
            specOwner.getAnnotations()
                .put(Comment.CommentOwner.EMAIL_HASH_ANNO, emailHash);
        }
        if (specOwner.getAnnotations() != null) {
            specOwner.getAnnotations().remove("Email");
        }
        return Mono.just(replyVo);
    }

    private Mono<OwnerInfo> getOwnerInfo(Comment.CommentOwner owner) {
        if (Comment.CommentOwner.KIND_EMAIL.equals(owner.getKind())) {
            return Mono.just(OwnerInfo.from(owner));
        }
        return userService.getUserOrGhost(owner.getName())
            .map(OwnerInfo::from);
    }

    private Mono<FieldSelector> fixedCommentFieldSelector(@Nullable Ref ref) {
        return Mono.fromSupplier(
                () -> {
                    var baseQuery = isNull("metadata.deletionTimestamp");
                    if (ref != null) {
                        baseQuery =
                            and(baseQuery,
                                equal("spec.subjectRef", Comment.toSubjectRefKey(ref)));
                    }
                    return baseQuery;
                })
            .flatMap(this::concatVisibleQuery)
            .map(FieldSelector::of);
    }

    private Mono<Query> concatVisibleQuery(Query query) {
        Assert.notNull(query, "The query must not be null");
        var approvedQuery = and(
            equal("spec.approved", BooleanUtils.TRUE),
            equal("spec.hidden", BooleanUtils.FALSE)
        );
        // we should list all comments that the user owns
        return getCurrentUserWithoutAnonymous()
            .map(username -> or(approvedQuery, equal("spec.owner",
                Comment.CommentOwner.ownerIdentity(User.KIND, username)))
            )
            .defaultIfEmpty(approvedQuery)
            .map(compositeQuery -> and(query, compositeQuery));
    }

    private Mono<FieldSelector> fixedReplyFieldSelector(String commentName) {
        Assert.notNull(commentName, "The commentName must not be null");
        // The comment name must be equal to the comment name of the reply
        // is approved and not hidden
        return Mono.fromSupplier(() -> and(
                equal("spec.commentName", commentName),
                isNull("metadata.deletionTimestamp")
            ))
            .flatMap(this::concatVisibleQuery)
            .map(FieldSelector::of);
    }

    Mono<String> getCurrentUserWithoutAnonymous() {
        return ReactiveSecurityContextHolder.getContext()
            .mapNotNull(SecurityContext::getAuthentication)
            .map(Principal::getName)
            .filter(username -> !AnonymousUserConst.PRINCIPAL.equals(username));
    }

    static Sort defaultCommentSort() {
        return Sort.by(Sort.Order.desc("spec.top"),
            Sort.Order.asc("spec.priority"),
            Sort.Order.desc("spec.creationTime"),
            Sort.Order.asc("metadata.name")
        );
    }

    static Sort defaultReplySort() {
        return Sort.by(Sort.Order.asc("spec.creationTime"),
            Sort.Order.asc("metadata.name")
        );
    }

    int pageNullSafe(Integer page) {
        return defaultIfNull(page, 1);
    }

    int sizeNullSafe(Integer size) {
        return defaultIfNull(size, DEFAULT_SIZE);
    }
}
