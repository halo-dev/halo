package run.halo.app.core.extension;

import static run.halo.app.core.extension.AnnotationSetting.KIND;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupKind;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "", version = "v1alpha1", kind = KIND,
    plural = "annotationsettings", singular = "annotationsetting")
public class AnnotationSetting extends AbstractExtension {
    public static final String TARGET_REF_LABEL = "halo.run/target-ref";

    public static final String KIND = "AnnotationSetting";

    private AnnotationSettingSpec spec;

    @Data
    public static class AnnotationSettingSpec {
        private GroupKind targetRef;
        private List<Object> formSchema;
    }
}
