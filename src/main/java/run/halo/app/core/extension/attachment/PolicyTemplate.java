package run.halo.app.core.extension.attachment;

import static run.halo.app.core.extension.attachment.PolicyTemplate.KIND;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Ref;

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

        private Ref settingRef;

    }

}
