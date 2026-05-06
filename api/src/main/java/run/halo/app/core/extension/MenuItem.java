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

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(
        group = "",
        version = "v1alpha1",
        kind = "MenuItem",
        plural = "menuitems",
        singular = "menuitem")
public class MenuItem extends AbstractExtension {

    public static final String REQUEST_TO_UPDATE_ANNO = "halo.run/request-to-update";

    @Schema(description = "The spec of menu item.", requiredMode = REQUIRED)
    @Nullable
    private MenuItemSpec spec;

    @Schema(description = "The status of menu item.")
    @Nullable
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
                uniqueItems = true,
                arraySchema = @Schema(description = "Children of this menu item"),
                schema = @Schema(description = "The name of menu item child"))
        private LinkedHashSet<String> children;

        @Schema(description = "Target reference. Like Category, Tag, Post or SinglePage")
        @Nullable
        private Ref targetRef;
    }

    @Data
    public static class MenuItemStatus {

        @Schema(description = "Calculated Display name of menu item.")
        @Nullable
        private String displayName;

        @Schema(description = "Calculated href of manu item.")
        @Nullable
        private String href;
    }
}
