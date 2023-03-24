package run.halo.app.core.extension.attachment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static run.halo.app.core.extension.attachment.PolicyTemplate.KIND;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = KIND,
    plural = "policytemplates", singular = "policytemplate")
public class PolicyTemplate extends AbstractExtension {

    public static final String KIND = "PolicyTemplate";

    private PolicyTemplateSpec spec;

    @Data
    public static class PolicyTemplateSpec {

        private String displayName;

        @Schema(requiredMode = REQUIRED)
        private String settingName;

    }

}
