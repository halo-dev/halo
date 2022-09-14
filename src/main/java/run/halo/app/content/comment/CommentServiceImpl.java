package run.halo.app.content.comment;

import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Comment;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
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
    private final ExtensionComponentsFinder extensionComponentsFinder;

    public CommentServiceImpl(ReactiveExtensionClient client,
        ExtensionComponentsFinder extensionComponentsFinder) {
        this.client = client;
        this.extensionComponentsFinder = extensionComponentsFinder;
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
                .flatMap(Function.identity())
                .collectList()
                .map(list -> new ListResult<>(comments.getPage(), comments.getSize(),
                    comments.getTotal(), list)
                )
            );
    }

    private Mono<ListedComment> toListedComment(Comment comment) {
        Comment.CommentOwner owner = comment.getSpec().getOwner();
        // not empty
        Mono<OwnerInfo> commentOwnerMono = getCommentOwnerInfo(owner);
        // not empty
        Mono<Extension> subjectMono =
            getCommentSubject(comment.getSpec().getSubjectRef());
        return Mono.zip(commentOwnerMono, subjectMono)
            .map(tuple -> new ListedComment(comment, tuple.getT1(), tuple.getT2()));
    }

    private Mono<OwnerInfo> getCommentOwnerInfo(Comment.CommentOwner owner) {
        if (User.KIND.equals(owner.getKind())) {
            return client.fetch(User.class, owner.getName())
                .map(OwnerInfo::from)
                .switchIfEmpty(Mono.error(
                    new IllegalStateException(
                        "Comment subject not found for: " + owner)));
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
