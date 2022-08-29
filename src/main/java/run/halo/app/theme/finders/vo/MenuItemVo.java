package run.halo.app.theme.finders.vo;

import java.util.LinkedHashSet;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import run.halo.app.core.extension.MenuItem;

/**
 * A value object for {@link MenuItem}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@Builder
public class MenuItemVo {

    String name;

    String parentName;

    String displayName;

    String href;

    Integer priority;

    LinkedHashSet<String> childrenNames;

    List<MenuItemVo> children;

    /**
     * Convert {@link MenuItem} to {@link MenuItemVo}.
     *
     * @param menuItem menu item extension
     * @return menu item value object
     */
    public static MenuItemVo from(MenuItem menuItem) {
        MenuItem.MenuItemStatus status = menuItem.getStatus();
        return MenuItemVo.builder()
            .name(menuItem.getMetadata().getName())
            .priority(menuItem.getSpec().getPriority())
            .childrenNames(menuItem.getSpec().getChildren())
            .displayName(status.getDisplayName())
            .href(status.getHref())
            .build();
    }
}
