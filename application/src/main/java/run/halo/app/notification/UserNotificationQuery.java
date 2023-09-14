package run.halo.app.notification;

import static java.util.Comparator.comparing;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.core.extension.endpoint.SortResolver;
import run.halo.app.core.extension.notification.Notification;
import run.halo.app.extension.Comparators;
import run.halo.app.extension.router.IListRequest;

/**
 * Notification query object for authenticated user.
 *
 * @author guqing
 * @since 2.10.0
 */
public class UserNotificationQuery extends IListRequest.QueryListRequest {

    private final ServerWebExchange exchange;

    public UserNotificationQuery(ServerWebExchange exchange) {
        super(exchange.getRequest().getQueryParams());
        this.exchange = exchange;
    }

    @Nullable
    public String getKeyword() {
        return StringUtils.defaultIfBlank(queryParams.getFirst("keyword"), null);
    }

    @Nullable
    @Schema(description = "true for unread, false for read, null for all")
    public Boolean getUnRead() {
        var unreadStr = queryParams.getFirst("unRead");
        return StringUtils.isBlank(unreadStr) ? null : Boolean.parseBoolean(unreadStr);
    }

    @Nullable
    @Schema(description = "Filter by notification reason")
    public String getReason() {
        return StringUtils.defaultIfBlank(queryParams.getFirst("reason"), null);
    }

    @ArraySchema(uniqueItems = true,
        arraySchema = @Schema(name = "sort",
            description = "Sort property and direction of the list result. Supported fields: "
                + "creationTimestamp"),
        schema = @Schema(description = "like field,asc or field,desc",
            implementation = String.class,
            example = "creationTimestamp,desc"))
    public Sort getSort() {
        return SortResolver.defaultInstance.resolve(exchange);
    }

    /**
     * Build a predicate from the query object.
     *
     * @return a predicate
     */
    public Predicate<Notification> toPredicate() {
        var unRead = getUnRead();
        var reason = getReason();
        Predicate<Notification> predicate = notification -> true;
        if (unRead != null) {
            predicate = predicate.and(notification
                -> notification.getSpec().isUnread() == unRead);
        }

        if (reason != null) {
            predicate = predicate.and(notification
                -> reason.equals(notification.getSpec().getReason()));
        }

        if (getKeyword() != null) {
            predicate = predicate.and(notification
                -> notification.getSpec().getTitle().contains(getKeyword())
                || notification.getSpec().getHtmlContent().contains(getKeyword())
                || notification.getSpec().getRawContent().contains(getKeyword()));
        }
        return predicate;
    }

    /**
     * Build a comparator from the query object.
     *
     * @return a comparator
     */
    public Comparator<Notification> toComparator() {
        var sort = getSort();
        var creationTimestampOrder = sort.getOrderFor("creationTimestamp");
        List<Comparator<Notification>> comparators = new ArrayList<>();
        if (creationTimestampOrder != null) {
            Comparator<Notification> comparator =
                comparing(notification -> notification.getMetadata().getCreationTimestamp());
            if (creationTimestampOrder.isDescending()) {
                comparator = comparator.reversed();
            }
            comparators.add(comparator);
        }

        comparators.add(Comparators.defaultComparator());
        return comparators.stream()
            .reduce(Comparator::thenComparing)
            .orElse(null);
    }
}