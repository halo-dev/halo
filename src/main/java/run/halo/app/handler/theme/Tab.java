package run.halo.app.handler.theme;

import lombok.Data;

import java.util.List;

/**
 * Theme configuration: Tab entity.
 *
 * @author johnniang
 * @date 4/10/19
 */
@Data
public class Tab {

    /**
     * Tab name.
     */
    private String name;

    /**
     * Tab label.
     */
    private String label;

    /**
     * Tab's items, default is empty list.
     */
    private List<Item> items;
}
