package run.halo.app.core.extension;

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
@GVK(group = "", version = "v1alpha1", kind = "Menu", plural = "menus", singular = "menu")
public class Menu extends AbstractExtension {

    @Schema(description = "The spec of menu.", required = true)
    private Spec spec;

    @Data
    @Schema(name = "MenuSpec")
    public static class Spec {

        @Schema(description = "The display name of the menu.", required = true)
        private String displayName;

        @Schema(description = "Names of menu children below this menu.")
        @ArraySchema(
            arraySchema = @Schema(description = "Menu items of this menu."),
            schema = @Schema(description = "Name of menu item.")
        )
        private LinkedHashSet<String> menuItems;

    }

}
