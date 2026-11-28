package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashSet;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.localization.Localizable;
import run.halo.app.localization.LocalizableNested;
import run.halo.app.localization.LocalizableResource;
import run.halo.app.localization.LocalizableSearchTitle;

/** Menu extension that groups ordered menu items for theme navigation. */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "", version = "v1alpha1", kind = "Menu", plural = "menus", singular = "menu")
@LocalizableResource(label = "core.menu.title")
public class Menu extends AbstractExtension {

    /** Desired menu metadata and ordered item references. */
    @Schema(requiredMode = REQUIRED)
    @LocalizableNested
    private Spec spec;

    /** Desired menu configuration. */
    @Data
    @Schema(name = "MenuSpec")
    public static class Spec {

        /** Display name shown for the menu. */
        @Schema(requiredMode = REQUIRED)
        @LocalizableSearchTitle
        @Localizable(key = "displayName", label = "core.localization.field.displayName", required = true)
        private String displayName;

        /** Ordered MenuItem metadata.name values included in this menu. */
        @Schema(
                deprecated = true,
                description = "Legacy ordered root MenuItem names. Menu hierarchy is now sourced from "
                        + "MenuItem.spec.menuName and MenuItem.spec.parent.")
        @ArraySchema(uniqueItems = true)
        private LinkedHashSet<String> menuItems;
    }
}
