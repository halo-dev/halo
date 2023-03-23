package run.halo.app.content.comment;

import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.plugin.ExtensionComponentsFinder;

/**
 * Comment service implementation.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class CommentServiceImpl implements CommentService {

    private final ReactiveExtensionClient client;
    private final UserService userService;
    private final ExtensionComponentsFinder extensionComponentsFinder;

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;
    private final CounterService counterService;

    public CommentServiceImpl(ReactiveExtensionClient client,
        UserService userService, ExtensionComponentsFinder extensionComponentsFinder,
        SystemConfigurableEnvironmentFetcher environmentFetcher,
        CounterService counterService) {
        this.client = client;
        this.userService = userService;
        this.extensionComponentsFinder = extensionComponentsFinder;
        this.environmentFetcher = environmentFetcher;
        this.counterService = counterService;
    }

    @Override
    public Mono<ListResult<ListedComment>> listComment(CommentQuery commentQuery) {
        Comparator<Comment> comparator =
            CommentSorter.from(commentQuery.getSort(), commentQuery.getSortOrder());
        return this.client.list(Comment.class, commentPredicate(commentQuery),
                comparator,
                commentQuery.getPage(), commentQuery.getSize())
            .flatMap(comments -> Flux.fromStream(comments.get()
                    .map(this::toListedComment))
                .concatMap(Function.identity())
                .collectList()
                .map(list -> new ListResult<>(comments.getPage(), comments.getSize(),
                    comments.getTotal(), list)
                )
            );
    }

    @Override
    public Mono<Comment> create(Comment comment) {
        return environmentFetcher.fetchComment()
            .flatMap(commentSetting -> {
                if (Boolean.FALSE.equals(commentSetting.getEnable())) {
                    return Mono.error(
                        new AccessDeniedException("The comment function has been turned off.",
                            "problemDetail.comment.turnedOff", null));
                }
                if (checkCommentOwner(comment, commentSetting.getSystemUserOnly())) {
                    return Mono.error(
                        new AccessDeniedException("Allow only system users to comment.",
                            "problemDetail.comment.systemUsersOnly", null));
                }

                if (comment.getSpec().getTop() == null) {
                    comment.getSpec().setTop(false);
                }
                if (comment.getSpec().getPriority() == null) {
                    comment.getSpec().setPriority(0);
                }
                comment.getSpec()
                    .setApproved(Boolean.FALSE.equals(commentSetting.getRequireReviewForNew()));

                if (BooleanUtils.isTrue(comment.getSpec().getApproved())
                    && comment.getSpec().getApprovedTime() == null) {
                    comment.getSpec().setApprovedTime(Instant.now());
                }

                if (comment.getSpec().getCreationTime() == null) {
                    comment.getSpec().setCreationTime(Instant.now());
                }

                comment.getSpec().setHidden(false);

                // return if the comment owner is not null
                if (comment.getSpec().getOwner() != null) {
                    return Mono.just(comment);
                }
                // populate owner from current user
                return fetchCurrentUser()
                    .map(this::toCommentOwner)
                    .map(owner -> {
                        comment.getSpec().setOwner(owner);
                        return comment;
                    })
                    .switchIfEmpty(
                        Mono.error(new IllegalStateException("The owner must not be null.")));
            })
            .flatMap(client::create);
    }

    private boolean checkCommentOwner(Comment comment, Boolean onlySystemUser) {
        Comment.CommentOwner owner = comment.getSpec().getOwner();
        if (Boolean.TRUE.equals(onlySystemUser)) {
            return owner != null && Comment.CommentOwner.KIND_EMAIL.equals(owner.getKind());
        }
        return false;
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

    private Mono<ListedComment> toListedComment(Comment comment) {
        ListedComment.ListedCommentBuilder commentBuilder = ListedComment.builder()
            .comment(comment);
        return Mono.just(commentBuilder)
            .flatMap(builder -> {
                Comment.CommentOwner owner = comment.getSpec().getOwner();
                // not empty
                return getCommentOwnerInfo(owner)
                    .map(builder::owner);
            })
            .flatMap(builder -> getCommentSubject(comment.getSpec().getSubjectRef())
                .map(subject -> {
                    builder.subject(subject);
                    return builder;
                })
                .switchIfEmpty(Mono.just(builder))
            )
            .map(ListedComment.ListedCommentBuilder::build)
            .flatMap(lc -> fetchStats(comment)
                .doOnNext(lc::setStats)
                .thenReturn(lc));
    }

    Mono<CommentStats> fetchStats(Comment comment) {
        Assert.notNull(comment, "The comment must not be null.");
        String name = comment.getMetadata().getName();
        return counterService.getByName(MeterUtils.nameOf(Comment.class, name))
            .map(counter -> CommentStats.builder()
                .upvote(counter.getUpvote())
                .build()
            )
            .defaultIfEmpty(CommentStats.empty());
    }

    private Mono<OwnerInfo> getCommentOwnerInfo(Comment.CommentOwner owner) {
        if (User.KIND.equals(owner.getKind())) {
            return userService.getUserOrGhost(owner.getName())
                .map(OwnerInfo::from);
        }
        if (Comment.CommentOwner.KIND_EMAIL.equals(owner.getKind())) {
            return Mono.just(OwnerInfo.from(owner));
        }
        throw new IllegalStateException(
            "Unsupported owner kind: " + owner.getKind());
    }

    @SuppressWarnings("unchecked")
    Mono<Extension> getCommentSubject(Ref ref) {
        return extensionComponentsFinder.getExtensions(CommentSubject.class)
            .stream()
            .filter(commentSubject -> commentSubject.supports(ref))
            .findFirst()
            .map(commentSubject -> commentSubject.get(ref.getName()))
            .orElseGet(Mono::empty);
    }

    Predicate<Comment> commentPredicate(CommentQuery query) {
        Predicate<Comment> predicate = comment -> true;

        String keyword = query.getKeyword();
        if (keyword != null) {
            predicate = predicate.and(comment -> {
                String raw = comment.getSpec().getRaw();
                return StringUtils.containsIgnoreCase(raw, keyword);
            });
        }

        Boolean approved = query.getApproved();
        if (approved != null) {
            predicate =
                predicate.and(comment -> Objects.equals(comment.getSpec().getApproved(), approved));
        }
        Boolean hidden = query.getHidden();
        if (hidden != null) {
            predicate =
                predicate.and(comment -> Objects.equals(comment.getSpec().getHidden(), hidden));
        }

        Boolean top = query.getTop();
        if (top != null) {
            predicate = predicate.and(comment -> Objects.equals(comment.getSpec().getTop(), top));
        }

        Boolean allowNotification = query.getAllowNotification();
        if (allowNotification != null) {
            predicate = predicate.and(
                comment -> Objects.equals(comment.getSpec().getAllowNotification(),
                    allowNotification));
        }

        String ownerKind = query.getOwnerKind();
        if (ownerKind != null) {
            predicate = predicate.and(comment -> {
                Comment.CommentOwner owner = comment.getSpec().getOwner();
                return Objects.equals(owner.getKind(), ownerKind);
            });
        }

        String ownerName = query.getOwnerName();
        if (ownerName != null) {
            predicate = predicate.and(comment -> {
                Comment.CommentOwner owner = comment.getSpec().getOwner();
                if (Comment.CommentOwner.KIND_EMAIL.equals(owner.getKind())) {
                    return Objects.equals(owner.getKind(), ownerKind)
                        && (StringUtils.containsIgnoreCase(owner.getName(), ownerName)
                        || StringUtils.containsIgnoreCase(owner.getDisplayName(), ownerName));
                }
                return Objects.equals(owner.getKind(), ownerKind)
                    && StringUtils.containsIgnoreCase(owner.getName(), ownerName);
            });
        }

        String subjectKind = query.getSubjectKind();
        if (subjectKind != null) {
            predicate = predicate.and(comment -> {
                Ref subjectRef = comment.getSpec().getSubjectRef();
                return Objects.equals(subjectRef.getKind(), subjectKind);
            });
        }

        String subjectName = query.getSubjectName();
        if (subjectName != null) {
            predicate = predicate.and(comment -> {
                Ref subjectRef = comment.getSpec().getSubjectRef();
                return Objects.equals(subjectRef.getKind(), subjectKind)
                    && StringUtils.containsIgnoreCase(subjectRef.getName(), subjectName);
            });
        }

        Predicate<Extension> labelAndFieldSelectorPredicate =
            labelAndFieldSelectorToPredicate(query.getLabelSelector(),
                query.getFieldSelector());
        return predicate.and(labelAndFieldSelectorPredicate);
    }
}
