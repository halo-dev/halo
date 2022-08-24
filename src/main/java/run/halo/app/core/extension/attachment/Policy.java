package run.halo.app.core.extension.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "storage.halo.run", version = "v1alpha1", kind = "Policy", plural = "policies",
    singular = "policy")
public class Policy extends AbstractExtension {

    @Schema(required = true)
    private PolicySpec spec;

    @Data
    public static class PolicySpec {

        @Schema(required = true, description = "Display name of policy")
        private String displayName;

        @Schema(description = "Reference name of Setting extension")
        private String settingRef;

        @Schema(description = "Reference name of ConfigMap extension")
        private String configMapRef;

    }

}
