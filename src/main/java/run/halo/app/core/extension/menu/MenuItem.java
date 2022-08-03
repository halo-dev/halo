package run.halo.app.core.extension.menu;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashSet;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

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

        @ArraySchema(
            arraySchema = @Schema(description = "Children of this menu item"),
            schema = @Schema(description = "The name of menu item child"))
        private LinkedHashSet<String> children;

        @Schema(description = "Category reference.")
        private MenuItemRef categoryRef;

        @Schema(description = "Tag reference.")
        private MenuItemRef tagRef;

        @Schema(description = "Post reference.")
        private MenuItemRef postRef;

        @Schema(description = "Page reference.")
        private MenuItemRef pageRef;

    }

    public static class AnchorAttributes {

        @Schema(description = "The href of anchor.")
        private String href;

        @Schema(description = "The target of anchor.")
        private String target;

    }

    @Data
    public static class MenuItemRef {

        @Schema(description = "Reference name.", required = true)
        private String name;

    }

    @Data
    public static class MenuItemStatus {

        @Schema(description = "Calculated Display name of menu item.")
        private String displayName;

        @Schema(description = "Calculated href of manu item.")
        private String href;

    }
}
