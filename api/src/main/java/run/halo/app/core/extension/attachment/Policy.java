package run.halo.app.core.extension.attachment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static run.halo.app.core.extension.attachment.Policy.KIND;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/** Storage policy extension that binds attachment uploads to a policy template and configuration. */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = KIND, plural = "policies", singular = "policy")
public class Policy extends AbstractExtension {
    public static final String POLICY_OWNER_LABEL = "storage.halo.run/policy-owner";

    public static final String KIND = "Policy";

    /** Desired storage policy metadata and template references. */
    @Schema(requiredMode = REQUIRED)
    private PolicySpec spec;

    /** Desired storage policy configuration. */
    @Data
    public static class PolicySpec {

        /** Display name shown for the storage policy. */
        @Schema(requiredMode = REQUIRED)
        private String displayName;

        /** PolicyTemplate metadata.name that provides the storage implementation. */
        @Schema(requiredMode = REQUIRED)
        private String templateName;

        /** ConfigMap metadata.name storing configuration values for this policy. */
        private String configMapName;
    }
}
