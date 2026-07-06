package run.halo.app.core.endpoint.console;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import run.halo.app.core.extension.content.Category;

/** Console Category tree node. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CategoryTreeNode")
public class CategoryTreeNode {

    @Schema(requiredMode = REQUIRED)
    private Category category;

    @Schema(requiredMode = REQUIRED)
    private List<CategoryTreeNode> children = new ArrayList<>();
}
