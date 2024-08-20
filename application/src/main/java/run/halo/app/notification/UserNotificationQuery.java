package run.halo.app.notification;

import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToListOptions;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.router.SortableRequest;
import run.halo.app.extension.router.selector.FieldSelector;

/**
 * Notification query object for authenticated user.
 *
 * @author guqing
 * @since 2.10.0
 */
public class UserNotificationQuery extends SortableRequest {

    private final String username;

    public UserNotificationQuery(ServerWebExchange exchange, String username) {
        super(exchange);
        this.username = username;
    }

    /**
     * Build a list options from the query object.
     */
    @Override
    public ListOptions toListOptions() {
        var listOptions =
            labelAndFieldSelectorToListOptions(getLabelSelector(), getFieldSelector());
        var filedQuery = listOptions.getFieldSelector().query();
        if (StringUtils.isNotBlank(username)) {
            filedQuery = and(filedQuery, equal("spec.recipient", username));
        }
        listOptions.setFieldSelector(FieldSelector.of(filedQuery));
        return listOptions;
    }
}
