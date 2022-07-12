package run.halo.app.core.extension;

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
@GVK(group = "", version = "v1alpha1", kind = "Setting",
    plural = "settings", singular = "setting")
public class Setting extends AbstractExtension {

    @Schema(required = true, minLength = 1)
    private List<SettingSpec> spec;

    @Data
    public static class SettingSpec {

        @Schema(required = true)
        private String group;

        private String label;

        private List<Object> formSchema;
    }
}
