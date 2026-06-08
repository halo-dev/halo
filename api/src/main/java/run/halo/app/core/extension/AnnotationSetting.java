package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static run.halo.app.core.extension.AnnotationSetting.KIND;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupKind;

/** Annotation setting extension that defines dynamic annotation forms for a target extension kind. */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "", version = "v1alpha1", kind = KIND, plural = "annotationsettings", singular = "annotationsetting")
public class AnnotationSetting extends AbstractExtension {
    public static final String TARGET_REF_LABEL = "halo.run/target-ref";

    public static final String KIND = "AnnotationSetting";

    /** Desired target resource kind and annotation form schema. */
    @Schema(requiredMode = REQUIRED)
    private AnnotationSettingSpec spec;

    /** Desired annotation form configuration for a target resource kind. */
    @Data
    public static class AnnotationSettingSpec {
        /** Target extension group and kind this annotation setting applies to. */
        @Schema(requiredMode = REQUIRED)
        private GroupKind targetRef;

        /** FormKit-compatible schema used to render annotation fields. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private List<Object> formSchema;
    }
}
