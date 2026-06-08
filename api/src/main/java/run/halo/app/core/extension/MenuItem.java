package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashSet;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Ref;

/** Menu item extension that describes a navigable item and its resolved rendering state. */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "", version = "v1alpha1", kind = "MenuItem", plural = "menuitems", singular = "menuitem")
public class MenuItem extends AbstractExtension {

    public static final String REQUEST_TO_UPDATE_ANNO = "halo.run/request-to-update";

    /** Desired menu item configuration, including label, URL, ordering, children, and target resource. */
    @Schema(requiredMode = REQUIRED)
    @Nullable
    private MenuItemSpec spec;

    /** Resolved display values calculated from the desired configuration and target resource. */
    @Nullable
    private MenuItemStatus status;

    /** HTML anchor target used when opening this menu item. */
    public enum Target {
        BLANK("_blank"),
        SELF("_self"),
        PARENT("_parent"),
        TOP("_top");

        /** HTML target attribute value. */
        private final String value;

        @JsonCreator
        Target(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    /** Desired menu item configuration. */
    @Data
    public static class MenuItemSpec {

        /** Display name shown for the menu item. */
        private String displayName;

        /** Direct URL used by the menu item. */
        private String href;

        /** HTML anchor target used by the menu item. */
        private Target target;

        /** Sorting priority. Higher values sort before lower values where priority ordering is applied. */
        private Integer priority;

        /** Child MenuItem metadata.name values shown under this item. */
        @ArraySchema(uniqueItems = true)
        private LinkedHashSet<String> children;

        /** Target extension reference, such as a Category, Tag, Post, or SinglePage. */
        @Nullable
        private Ref targetRef;
    }

    /** Resolved menu item values used for rendering. */
    @Data
    public static class MenuItemStatus {

        /** Calculated display name after resolving targetRef, falling back to spec.displayName. */
        @Nullable
        private String displayName;

        /** Calculated href after resolving targetRef, falling back to spec.href. */
        @Nullable
        private String href;
    }
}
