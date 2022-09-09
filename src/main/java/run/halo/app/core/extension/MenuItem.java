package run.halo.app.core.extension;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashSet;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Ref;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "", version = "v1alpha1", kind = "MenuItem",
    plural = "menuitems", singular = "menuitem")
public class MenuItem extends AbstractExtension {

    @Schema(description = "The spec of menu item.", required = true)
    private MenuItemSpec spec;

    @Schema(description = "The status of menu item.")
    private MenuItemStatus status;

    @Data
    public static class MenuItemSpec {

        @Schema(description = "The display name of menu item.")
        private String displayName;

        @Schema(description = "The href of this menu item.")
        private String href;

        @Schema(description = "The priority is for ordering.")
        private Integer priority;

        @ArraySchema(
            arraySchema = @Schema(description = "Children of this menu item"),
            schema = @Schema(description = "The name of menu item child"))
        private LinkedHashSet<String> children;

        @Schema(description = "Category reference.")
        private Ref categoryRef;

        @Schema(description = "Tag reference.")
        private Ref tagRef;

        @Schema(description = "Post reference.")
        private Ref postRef;

        @Schema(description = "SinglePage reference.")
        private Ref singlePageRef;

    }

    @Data
    public static class MenuItemStatus {

        @Schema(description = "Calculated Display name of menu item.")
        private String displayName;

        @Schema(description = "Calculated href of manu item.")
        private String href;

    }
}
