package run.halo.app.theme.finders.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.MetadataOperator;

/**
 * A value object for {@link MenuItem}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@ToString
@Builder
public class MenuItemVo implements VisualizableTreeNode<MenuItemVo>, ExtensionVoOperator {

    MetadataOperator metadata;

    MenuItem.MenuItemSpec spec;

    MenuItem.MenuItemStatus status;

    List<MenuItemVo> children;

    String parentName;

    /**
     * Gets menu item's display name.
     */
    public String getDisplayName() {
        if (status != null && StringUtils.isNotBlank(status.getDisplayName())) {
            return status.getDisplayName();
        }
        return spec.getDisplayName();
    }

    /**
     * Convert {@link MenuItem} to {@link MenuItemVo}.
     *
     * @param menuItem menu item extension
     * @return menu item value object
     */
    public static MenuItemVo from(MenuItem menuItem) {
        MenuItem.MenuItemStatus status = menuItem.getStatus();
        return MenuItemVo.builder()
            .metadata(menuItem.getMetadata())
            .spec(menuItem.getSpec())
            .status(status)
            .children(List.of())
            .build();
    }

    @Override
    public String nodeText() {
        return getDisplayName();
    }
}
