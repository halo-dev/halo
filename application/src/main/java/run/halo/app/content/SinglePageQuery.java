package run.halo.app.content;

import static java.util.Comparator.comparing;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.extension.router.QueryParamBuildUtil.sortParameter;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.endpoint.SortResolver;
import run.halo.app.extension.Comparators;
import run.halo.app.extension.router.IListRequest;

/**
 * Query parameter for {@link SinglePage} list.
 *
 * @author guqing
 * @since 2.0.0
 */
public class SinglePageQuery extends IListRequest.QueryListRequest {

    private final ServerWebExchange exchange;

    public SinglePageQuery(ServerRequest request) {
        super(request.queryParams());
        this.exchange = request.exchange();
    }

    @Nullable
    @Schema(name = "contributor")
    public Set<String> getContributors() {
        List<String> contributorList = queryParams.get("contributor");
        return contributorList == null ? null : Set.copyOf(contributorList);
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
    @Schema(description = "SinglePages filtered by keyword.")
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

    /**
     * Build a comparator for {@link SinglePageQuery}.
     *
     * @return comparator
     */
    public Comparator<SinglePage> toComparator() {
        var sort = getSort();
        var creationTimestampOrder = sort.getOrderFor("creationTimestamp");
        List<Comparator<SinglePage>> comparators = new ArrayList<>();
        if (creationTimestampOrder != null) {
            Comparator<SinglePage> comparator =
                comparing(page -> page.getMetadata().getCreationTimestamp());
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
            Comparator<SinglePage> comparator =
                comparing(page -> page.getSpec().getPublishTime(), nullsComparator);
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
     * Build a predicate for {@link SinglePageQuery}.
     *
     * @return predicate
     */
    public Predicate<SinglePage> toPredicate() {
        Predicate<SinglePage> paramPredicate = singlePage -> contains(getContributors(),
            singlePage.getStatusOrDefault().getContributors());

        String keyword = getKeyword();
        if (keyword != null) {
            paramPredicate = paramPredicate.and(page -> {
                String excerpt = page.getStatusOrDefault().getExcerpt();
                return StringUtils.containsIgnoreCase(excerpt, keyword)
                    || StringUtils.containsIgnoreCase(page.getSpec().getSlug(), keyword)
                    || StringUtils.containsIgnoreCase(page.getSpec().getTitle(), keyword);
            });
        }

        Post.PostPhase publishPhase = getPublishPhase();
        if (publishPhase != null) {
            paramPredicate = paramPredicate.and(page -> {
                if (Post.PostPhase.PENDING_APPROVAL.equals(publishPhase)) {
                    return !page.isPublished()
                        && Post.PostPhase.PENDING_APPROVAL.name()
                        .equalsIgnoreCase(page.getStatusOrDefault().getPhase());
                }
                // published
                if (Post.PostPhase.PUBLISHED.equals(publishPhase)) {
                    return page.isPublished();
                }
                // draft
                return !page.isPublished();
            });
        }

        Post.VisibleEnum visible = getVisible();
        if (visible != null) {
            paramPredicate =
                paramPredicate.and(post -> visible.equals(post.getSpec().getVisible()));
        }

        Predicate<SinglePage> predicate = labelAndFieldSelectorToPredicate(getLabelSelector(),
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

    public static void buildParameters(Builder builder) {
        IListRequest.buildParameters(builder);
        builder.parameter(sortParameter())
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("contributor")
                .description("SinglePages filtered by contributor.")
                .implementationArray(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("publishPhase")
                .description("SinglePages filtered by publish phase.")
                .implementation(Post.PostPhase.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("visible")
                .description("SinglePages filtered by visibility.")
                .implementation(Post.VisibleEnum.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("keyword")
                .description("SinglePages filtered by keyword.")
                .implementation(String.class)
                .required(false));
        ;
    }
}
