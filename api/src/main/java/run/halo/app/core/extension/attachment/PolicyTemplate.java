package run.halo.app.core.extension.attachment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static run.halo.app.core.extension.attachment.PolicyTemplate.KIND;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/** Storage policy template extension that declares an attachment storage implementation and its settings form. */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(
        group = Constant.GROUP,
        version = Constant.VERSION,
        kind = KIND,
        plural = "policytemplates",
        singular = "policytemplate")
public class PolicyTemplate extends AbstractExtension {

    public static final String KIND = "PolicyTemplate";

    /** Desired template metadata and setting reference. */
    private PolicyTemplateSpec spec;

    /** Desired storage policy template metadata. */
    @Data
    public static class PolicyTemplateSpec {

        /** Display name shown for the storage policy template. */
        private String displayName;

        /** Setting metadata.name used to render configuration fields for policies created from this template. */
        @Schema(requiredMode = REQUIRED)
        private String settingName;
    }
}
