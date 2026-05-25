package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static run.halo.app.extension.GroupVersionKind.fromExtension;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;

/**
 * Setting extension that describes one or more configuration forms and their schema.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "", version = "v1alpha1", kind = Setting.KIND, plural = "settings", singular = "setting")
public class Setting extends AbstractExtension {

    public static final String KIND = "Setting";

    public static final GroupVersionKind GVK = fromExtension(Setting.class);

    /** Desired configuration form groups exposed by this setting definition. */
    @Schema(requiredMode = REQUIRED)
    private SettingSpec spec;

    /** Collection of form groups that make up this setting definition. */
    @Data
    public static class SettingSpec {

        /** Form groups rendered for this setting definition. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private List<SettingForm> forms;
    }

    /** A single form group in a setting definition. */
    @Data
    public static class SettingForm {

        /** Stable form group name used to store submitted values. */
        @Schema(requiredMode = REQUIRED)
        private String group;

        /** Display label shown for this form group. */
        private String label;

        /** FormKit-compatible schema used to render the form fields. */
        @Schema(requiredMode = REQUIRED)
        private List<Object> formSchema;
    }
}
