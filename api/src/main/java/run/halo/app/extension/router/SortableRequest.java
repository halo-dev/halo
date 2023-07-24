package run.halo.app.extension.router;

import static run.halo.app.extension.Comparators.compareCreationTimestamp;
import static run.halo.app.extension.Comparators.compareName;
import static run.halo.app.extension.Comparators.nullsComparator;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.core.extension.endpoint.SortResolver;
import run.halo.app.extension.Extension;

public class SortableRequest extends IListRequest.QueryListRequest {

    protected final ServerWebExchange exchange;

    public SortableRequest(ServerWebExchange exchange) {
        super(exchange.getRequest().getQueryParams());
        this.exchange = exchange;
    }

    @ArraySchema(uniqueItems = true,
        arraySchema = @Schema(name = "sort",
            description = "Sort property and direction of the list result. Support sorting based "
                + "on attribute name path."),
        schema = @Schema(description = "like field,asc or field,desc",
            implementation = String.class,
            example = "metadata.creationTimestamp,desc"))
    public Sort getSort() {
        return SortResolver.defaultInstance.resolve(exchange);
    }

    /**
     * Build predicate from query params, default is label and field selector, you can
     * override this method to change it.
     *
     * @return predicate
     */
    public <T extends Extension> Predicate<T> toPredicate() {
        return labelAndFieldSelectorToPredicate(getLabelSelector(), getFieldSelector());
    }

    /**
     * Build comparator from sort.
     *
     * @param <T> Extension type
     * @return comparator
     */
    public <T extends Extension> Comparator<T> toComparator() {
        var sort = getSort();
        var fallbackComparator = Stream.<Comparator<T>>of(
            compareCreationTimestamp(false),
            compareName(true)
        );
        var comparatorStream = sort.stream().map(order -> {
            var property = order.getProperty();
            var direction = order.getDirection();
            Function<T, Object> function = extension -> {
                BeanWrapper beanWrapper = new BeanWrapperImpl(extension);
                return beanWrapper.getPropertyValue(property);
            };
            var comparator =
                Comparator.comparing(function, nullsComparator(direction.isAscending()));
            if (direction.isDescending()) {
                comparator = comparator.reversed();
            }
            return comparator;
        });
        return Stream.concat(comparatorStream, fallbackComparator)
            .reduce(Comparator::thenComparing)
            .orElse(null);
    }
}
