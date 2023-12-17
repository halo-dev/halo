package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * {@link Setting} is a custom extension to generate forms based on configuration.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "", version = "v1alpha1", kind = Setting.KIND,
    plural = "settings", singular = "setting")
public class Setting extends AbstractExtension {

    public static final String KIND = "Setting";

    @Schema(requiredMode = REQUIRED)
    private SettingSpec spec;

    @Data
    public static class SettingSpec {

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private List<SettingForm> forms;
    }

    @Data
    public static class SettingForm {

        @Schema(requiredMode = REQUIRED)
        private String group;

        private String label;

        @Schema(requiredMode = REQUIRED)
        private List<Object> formSchema;
    }
}
