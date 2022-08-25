package run.halo.app.theme.finders.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import run.halo.app.core.extension.Menu;

/**
 * A value object for {@link Menu}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
public class MenuVo {

    String name;

    String displayName;

    @JsonIgnore
    LinkedHashSet<String> menuItemNames;

    @With
    List<MenuItemVo> menuItems;

    /**
     * Convert {@link Menu} to {@link MenuVo}.
     *
     * @param menu menu extension
     * @return menu value object
     */
    public static MenuVo from(Menu menu) {
        return builder()
            .name(menu.getMetadata().getName())
            .displayName(menu.getSpec().getDisplayName())
            .menuItemNames(menu.getSpec().getMenuItems())
            .build();
    }
}
