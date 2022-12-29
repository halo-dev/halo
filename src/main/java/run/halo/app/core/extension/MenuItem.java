package run.halo.app.core.extension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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

    public enum Target {
        BLANK("_blank"),
        SELF("_self"),
        PARENT("_parent"),
        TOP("_top");

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

    @Data
    public static class MenuItemSpec {

        @Schema(description = "The display name of menu item.")
        private String displayName;

        @Schema(description = "The href of this menu item.")
        private String href;

        @Schema(description = "The <a> target attribute of this menu item.")
        private Target target;

        @Schema(description = "The priority is for ordering.")
        private Integer priority;

        @ArraySchema(
            arraySchema = @Schema(description = "Children of this menu item"),
            schema = @Schema(description = "The name of menu item child"))
        private LinkedHashSet<String> children;

        @Schema(description = "Target reference. Like Category, Tag, Post or SinglePage")
        private Ref targetRef;

    }

    @Data
    public static class MenuItemStatus {

        @Schema(description = "Calculated Display name of menu item.")
        private String displayName;

        @Schema(description = "Calculated href of manu item.")
        private String href;

    }
}
