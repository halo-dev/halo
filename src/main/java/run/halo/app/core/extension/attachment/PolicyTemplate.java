package run.halo.app.core.extension.attachment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Ref;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "storage.halo.run", version = "v1alpha1", kind = "PolicyTemplate",
    plural = "policytemplates", singular = "policytemplate")
public class PolicyTemplate extends AbstractExtension {

    private PolicyTemplateSpec spec;

    @Data
    public static class PolicyTemplateSpec {

        private String displayName;

        private Ref settingRef;

    }

}
