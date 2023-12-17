package run.halo.app.content.comment;

import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.endpoint.SortResolver;
import run.halo.app.extension.Comparators;
import run.halo.app.extension.Extension;
import run.halo.app.extension.Ref;
import run.halo.app.extension.router.IListRequest;

/**
 * Query criteria for comment list.
 *
 * @author guqing
 * @since 2.0.0
 */
public class CommentQuery extends IListRequest.QueryListRequest {

    private final ServerWebExchange exchange;
    static final Function<Comment, Instant> LAST_REPLY_TIME_FUNC =
        comment -> {
            Instant lastReplyTime = comment.getStatusOrDefault().getLastReplyTime();
            return Optional.ofNullable(lastReplyTime)
                .orElse(comment.getSpec().getCreationTime());
        };

    public CommentQuery(ServerRequest request) {
        super(request.queryParams());
        this.exchange = request.exchange();
    }

    @Schema(description = "Comments filtered by keyword.")
    public String getKeyword() {
        String keyword = queryParams.getFirst("keyword");
        return StringUtils.isBlank(keyword) ? null : keyword;
    }

    @Schema(description = "Comments approved.")
    public Boolean getApproved() {
        return convertBooleanOrNull(queryParams.getFirst("approved"));
    }

    @Schema(description = "The comment is hidden from the theme side.")
    public Boolean getHidden() {
        return convertBooleanOrNull(queryParams.getFirst("hidden"));
    }

    @Schema(description = "Send notifications when there are new replies.")
    public Boolean getAllowNotification() {
        return convertBooleanOrNull(queryParams.getFirst("allowNotification"));
    }

    @Schema(description = "Comment top display.")
    public Boolean getTop() {
        return convertBooleanOrNull(queryParams.getFirst("top"));
    }

    @Schema(description = "Commenter kind.")
    public String getOwnerKind() {
        String ownerKind = queryParams.getFirst("ownerKind");
        return StringUtils.isBlank(ownerKind) ? null : ownerKind;
    }

    @Schema(description = "Commenter name.")
    public String getOwnerName() {
        String ownerName = queryParams.getFirst("ownerName");
        return StringUtils.isBlank(ownerName) ? null : ownerName;
    }

    @Schema(description = "Comment subject kind.")
    public String getSubjectKind() {
        String subjectKind = queryParams.getFirst("subjectKind");
        return StringUtils.isBlank(subjectKind) ? null : subjectKind;
    }

    @Schema(description = "Comment subject name.")
    public String getSubjectName() {
        String subjectName = queryParams.getFirst("subjectName");
        return StringUtils.isBlank(subjectName) ? null : subjectName;
    }

    @ArraySchema(uniqueItems = true,
        arraySchema = @Schema(name = "sort",
            description = "Sort property and direction of the list result. Supported fields: "
                + "creationTimestamp,replyCount,lastReplyTime"),
        schema = @Schema(description = "like field,asc or field,desc",
            implementation = String.class,
            example = "creationTimestamp,desc"))
    public Sort getSort() {
        return SortResolver.defaultInstance.resolve(exchange);
    }

    /**
     * Build a comparator from the query.
     *
     * @return comparator
     */
    public Comparator<Comment> toComparator() {
        var sort = getSort();
        var creationTimestampOrder = sort.getOrderFor("creationTimestamp");
        List<Comparator<Comment>> comparators = new ArrayList<>();
        if (creationTimestampOrder != null) {
            Comparator<Comment> comparator =
                comparing(comment -> comment.getMetadata().getCreationTimestamp());
            if (creationTimestampOrder.isDescending()) {
                comparator = comparator.reversed();
            }
            comparators.add(comparator);
        }

        var replyCountOrder = sort.getOrderFor("replyCount");
        if (replyCountOrder != null) {
            Comparator<Comment> comparator = comparing(
                comment -> defaultIfNull(comment.getStatusOrDefault().getReplyCount(), 0));
            if (replyCountOrder.isDescending()) {
                comparator = comparator.reversed();
            }
            comparators.add(comparator);
        }

        var lastReplyTimeOrder = sort.getOrderFor("lastReplyTime");
        if (lastReplyTimeOrder == null) {
            lastReplyTimeOrder = new Sort.Order(Sort.Direction.DESC, "lastReplyTime");
        }
        Comparator<Comment> comparator = comparing(LAST_REPLY_TIME_FUNC,
            Comparators.nullsComparator(lastReplyTimeOrder.isAscending()));
        if (lastReplyTimeOrder.isDescending()) {
            comparator = comparator.reversed();
        }
        comparators.add(comparator);
        comparators.add(Comparators.compareCreationTimestamp(false));
        comparators.add(Comparators.compareName(true));
        return comparators.stream()
            .reduce(Comparator::thenComparing)
            .orElse(null);
    }

    /**
     * Build a predicate from the query.
     *
     * @return predicate
     */
    Predicate<Comment> toPredicate() {
        Predicate<Comment> predicate = comment -> true;

        String keyword = getKeyword();
        if (keyword != null) {
            predicate = predicate.and(comment -> {
                String raw = comment.getSpec().getRaw();
                return StringUtils.containsIgnoreCase(raw, keyword);
            });
        }

        Boolean approved = getApproved();
        if (approved != null) {
            predicate =
                predicate.and(comment -> Objects.equals(comment.getSpec().getApproved(), approved));
        }
        Boolean hidden = getHidden();
        if (hidden != null) {
            predicate =
                predicate.and(comment -> Objects.equals(comment.getSpec().getHidden(), hidden));
        }

        Boolean top = getTop();
        if (top != null) {
            predicate = predicate.and(comment -> Objects.equals(comment.getSpec().getTop(), top));
        }

        Boolean allowNotification = getAllowNotification();
        if (allowNotification != null) {
            predicate = predicate.and(
                comment -> Objects.equals(comment.getSpec().getAllowNotification(),
                    allowNotification));
        }

        String ownerKind = getOwnerKind();
        if (ownerKind != null) {
            predicate = predicate.and(comment -> {
                Comment.CommentOwner owner = comment.getSpec().getOwner();
                return Objects.equals(owner.getKind(), ownerKind);
            });
        }

        String ownerName = getOwnerName();
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

        String subjectKind = getSubjectKind();
        if (subjectKind != null) {
            predicate = predicate.and(comment -> {
                Ref subjectRef = comment.getSpec().getSubjectRef();
                return Objects.equals(subjectRef.getKind(), subjectKind);
            });
        }

        String subjectName = getSubjectName();
        if (subjectName != null) {
            predicate = predicate.and(comment -> {
                Ref subjectRef = comment.getSpec().getSubjectRef();
                return Objects.equals(subjectRef.getKind(), subjectKind)
                    && StringUtils.containsIgnoreCase(subjectRef.getName(), subjectName);
            });
        }

        Predicate<Extension> labelAndFieldSelectorPredicate =
            labelAndFieldSelectorToPredicate(getLabelSelector(),
                getFieldSelector());
        return predicate.and(labelAndFieldSelectorPredicate);
    }

    private Boolean convertBooleanOrNull(String value) {
        return StringUtils.isBlank(value) ? null : Boolean.parseBoolean(value);
    }
}
