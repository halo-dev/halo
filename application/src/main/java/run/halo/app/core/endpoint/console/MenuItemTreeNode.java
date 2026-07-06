package run.halo.app.core.endpoint.console;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import run.halo.app.core.extension.MenuItem;

/** Console MenuItem tree node. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MenuItemTreeNode")
public class MenuItemTreeNode {

    @Schema(requiredMode = REQUIRED)
    private MenuItem menuItem;

    @Schema(requiredMode = REQUIRED)
    private List<MenuItemTreeNode> children = new ArrayList<>();
}
