package run.halo.app.theme.finders.impl;


import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.core.extension.content.Comment.CommentOwner.ownerIdentity;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.isNull;
import static run.halo.app.extension.index.query.QueryFactory.or;

import com.google.common.hash.Hashing;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.comment.OwnerInfo;
import run.halo.app.core.counter.CounterService;
import run.halo.app.core.counter.MeterUtils;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.infra.AnonymousUserConst;
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

    private static final String COMMENT_VIEW_PERMISSION = "role-template-view-comments";

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
        return populateCommentListOptions(ref)
            .flatMap(listOptions -> client.listBy(Comment.class, listOptions, pageRequest))
            .flatMap(listResult -> Flux.fromStream(listResult.get())
                .map(this::toCommentVo)
                .flatMapSequential(Function.identity())
                .collectList()
                .map(commentVos -> new ListResult<>(listResult.getPage(),
                    listResult.getSize(),
                    listResult.getTotal(),
                    commentVos)
                )
            )
            .defaultIfEmpty(ListResult.emptyResult());
    }

    @Override
    public Mono<ListResult<CommentWithReplyVo>> convertToWithReplyVo(ListResult<CommentVo> comments,
        int replySize) {
        return Flux.fromIterable(comments.getItems())
            .flatMapSequential(commentVo -> {
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
        // check comment
        return client.get(Comment.class, commentName)
            .flatMap(this::populateReplyListOptions)
            .flatMap(listOptions -> {
                var pageRequest = Optional.ofNullable(pageParam)
                    .map(page -> page.withSort(page.getSort().and(defaultReplySort())))
                    .orElse(PageRequestImpl.ofSize(0));
                return client.listBy(Reply.class, listOptions, pageRequest)
                    .flatMap(list -> Flux.fromStream(list.get().map(this::toReplyVo))
                        .flatMapSequential(Function.identity())
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
            var emailHash = Hashing.sha256()
                .hashString(email.toLowerCase(), UTF_8)
                .toString();
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
            var emailHash = Hashing.sha256()
                .hashString(email.toLowerCase(), UTF_8)
                .toString();
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

    private Mono<ListOptions> populateCommentListOptions(@Nullable Ref ref) {
        return populateVisibleListOptions(null)
            .doOnNext(builder -> {
                if (ref != null) {
                    builder.andQuery(
                        equal("spec.subjectRef", Comment.toSubjectRefKey(ref)));
                }
            })
            .map(ListOptions.ListOptionsBuilder::build);
    }

    private Mono<ListOptions.ListOptionsBuilder> populateVisibleListOptions(
        @Nullable Comment comment) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName)
            .defaultIfEmpty(AnonymousUserConst.PRINCIPAL)
            .zipWith(userService.hasSufficientRoles(Set.of(COMMENT_VIEW_PERMISSION))
                .defaultIfEmpty(false))
            .flatMap(tuple2 -> {
                var username = tuple2.getT1();
                var hasViewPermission = tuple2.getT2();
                var commentHidden = false;
                var isCommentOwner = false;
                if (comment != null) {
                    commentHidden = Boolean.TRUE.equals(comment.getSpec().getHidden());
                    var owner = comment.getSpec().getOwner();
                    isCommentOwner = owner != null && Objects.equals(
                        ownerIdentity(owner.getKind(), owner.getName()),
                        ownerIdentity(User.KIND, username)
                    );
                    boolean hasPermission =
                        (!commentHidden) || (hasViewPermission || isCommentOwner);
                    if (ExtensionUtil.isDeleted(comment) || !hasPermission) {
                        return Mono.error(new ServerWebInputException(
                            "The comment was not found, hidden or deleted."
                        ));
                    }
                }

                var builder = ListOptions.builder();
                builder.andQuery(isNull("metadata.deletionTimestamp"));
                var visibleQuery = and(
                    equal("spec.hidden", BooleanUtils.FALSE),
                    equal("spec.approved", BooleanUtils.TRUE)
                );

                var isAnonymous = AnonymousUserConst.isAnonymousUser(username);
                if (isAnonymous) {
                    builder.andQuery(visibleQuery);
                } else if (!(hasViewPermission || (commentHidden && isCommentOwner))) {
                    builder.andQuery(or(
                        equal("spec.owner", ownerIdentity(User.KIND, username)),
                        visibleQuery
                    ));
                }
                // View all replies if the user is not an anonymous user, has view permission
                // or is the comment owner.
                return Mono.just(builder);
            });
    }

    private Mono<ListOptions> populateReplyListOptions(Comment comment) {
        // The comment name must be equal to the comment name of the reply
        // is approved and not hidden
        return populateVisibleListOptions(comment)
            .doOnNext(builder ->
                builder.andQuery(equal("spec.commentName", comment.getMetadata().getName()))
            )
            .map(ListOptions.ListOptionsBuilder::build);
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
