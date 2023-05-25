package run.halo.app.content;

import static java.util.Comparator.comparing;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.endpoint.SortResolver;
import run.halo.app.extension.Comparators;
import run.halo.app.extension.router.IListRequest;

/**
 * A query object for {@link Post} list.
 *
 * @author guqing
 * @since 2.0.0
 */
public class PostQuery extends IListRequest.QueryListRequest {

    private final ServerWebExchange exchange;

    public PostQuery(ServerRequest request) {
        super(request.queryParams());
        this.exchange = request.exchange();
    }

    @Nullable
    @Schema(name = "contributor")
    public Set<String> getContributors() {
        return listToSet(queryParams.get("contributor"));
    }

    @Nullable
    @Schema(name = "category")
    public Set<String> getCategories() {
        return listToSet(queryParams.get("category"));
    }

    @Nullable
    @Schema(name = "tag")
    public Set<String> getTags() {
        return listToSet(queryParams.get("tag"));
    }

    @Nullable
    public Post.PostPhase getPublishPhase() {
        String publishPhase = queryParams.getFirst("publishPhase");
        return Post.PostPhase.from(publishPhase);
    }

    @Nullable
    public Post.VisibleEnum getVisible() {
        String visible = queryParams.getFirst("visible");
        return Post.VisibleEnum.from(visible);
    }

    @Nullable
    @Schema(description = "Posts filtered by keyword.")
    public String getKeyword() {
        return StringUtils.defaultIfBlank(queryParams.getFirst("keyword"), null);
    }

    @ArraySchema(uniqueItems = true,
        arraySchema = @Schema(name = "sort",
            description = "Sort property and direction of the list result. Supported fields: "
                + "creationTimestamp,publishTime"),
        schema = @Schema(description = "like field,asc or field,desc",
            implementation = String.class,
            example = "creationTimestamp,desc"))
    public Sort getSort() {
        return SortResolver.defaultInstance.resolve(exchange);
    }

    @Nullable
    private Set<String> listToSet(List<String> param) {
        return param == null ? null : Set.copyOf(param);
    }

    /**
     * Build a comparator from the query object.
     *
     * @return a comparator
     */
    public Comparator<Post> toComparator() {
        var sort = getSort();
        var creationTimestampOrder = sort.getOrderFor("creationTimestamp");
        List<Comparator<Post>> comparators = new ArrayList<>();
        if (creationTimestampOrder != null) {
            Comparator<Post> comparator =
                comparing(post -> post.getMetadata().getCreationTimestamp());
            if (creationTimestampOrder.isDescending()) {
                comparator = comparator.reversed();
            }
            comparators.add(comparator);
        }

        var publishTimeOrder = sort.getOrderFor("publishTime");
        if (publishTimeOrder != null) {
            Comparator<Object> nullsComparator = publishTimeOrder.isAscending()
                ? org.springframework.util.comparator.Comparators.nullsLow()
                : org.springframework.util.comparator.Comparators.nullsHigh();
            Comparator<Post> comparator =
                comparing(post -> post.getSpec().getPublishTime(), nullsComparator);
            if (publishTimeOrder.isDescending()) {
                comparator = comparator.reversed();
            }
            comparators.add(comparator);
        }
        comparators.add(Comparators.compareCreationTimestamp(false));
        comparators.add(Comparators.compareName(true));
        return comparators.stream()
            .reduce(Comparator::thenComparing)
            .orElse(null);
    }

    /**
     * Build a predicate from the query object.
     *
     * @return a predicate
     */
    public Predicate<Post> toPredicate() {
        Predicate<Post> paramPredicate = post ->
            contains(getCategories(), post.getSpec().getCategories())
                && contains(getTags(), post.getSpec().getTags())
                && contains(getContributors(), post.getStatusOrDefault().getContributors());

        String keyword = getKeyword();
        if (keyword != null) {
            paramPredicate = paramPredicate.and(post -> {
                String excerpt = post.getStatusOrDefault().getExcerpt();
                return StringUtils.containsIgnoreCase(excerpt, keyword)
                    || StringUtils.containsIgnoreCase(post.getSpec().getSlug(), keyword)
                    || StringUtils.containsIgnoreCase(post.getSpec().getTitle(), keyword);
            });
        }

        Post.PostPhase publishPhase = getPublishPhase();
        if (publishPhase != null) {
            paramPredicate = paramPredicate.and(post -> {
                if (Post.PostPhase.PENDING_APPROVAL.equals(publishPhase)) {
                    return !post.isPublished()
                        && Post.PostPhase.PENDING_APPROVAL.name()
                        .equalsIgnoreCase(post.getStatusOrDefault().getPhase());
                }
                // published
                if (Post.PostPhase.PUBLISHED.equals(publishPhase)) {
                    return post.isPublished();
                }
                // draft
                return !post.isPublished();
            });
        }

        Post.VisibleEnum visible = getVisible();
        if (visible != null) {
            paramPredicate =
                paramPredicate.and(post -> visible.equals(post.getSpec().getVisible()));
        }

        Predicate<Post> predicate = labelAndFieldSelectorToPredicate(getLabelSelector(),
            getFieldSelector());
        return predicate.and(paramPredicate);
    }

    boolean contains(Collection<String> left, List<String> right) {
        // parameter is null, it means that ignore this condition
        if (left == null) {
            return true;
        }
        // else, it means that right is empty
        if (left.isEmpty()) {
            return right.isEmpty();
        }
        if (right == null) {
            return false;
        }
        return right.stream().anyMatch(left::contains);
    }
}
