package run.halo.app.core.extension.attachment;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Ref;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "storage.halo.run", version = "v1alpha1", kind = "Attachment", plural = "attachments"
    , singular = "attachment")
public class Attachment extends AbstractExtension {

    @Schema(required = true)
    private AttachmentSpec spec;

    private AttachmentStatus status;

    @Data
    public static class AttachmentSpec {

        @Schema(description = "Display name of attachment")
        private String displayName;

        @Schema(description = "Reference of Group")
        private Ref groupRef;

        @Schema(description = "Reference of Policy")
        private Ref policyRef;

        @Schema(description = "Reference of User who uploads the attachment")
        private Ref uploadedBy;

        @Schema(description = "Media type of attachment")
        private String mediaType;

        @Schema(description = "Size of attachment. Unit is Byte", minimum = "0")
        private Long size;

        @ArraySchema(
            arraySchema = @Schema(description = "Tags of attachment"),
            schema = @Schema(description = "Tag name"))
        private Set<String> tags;

    }

    @Data
    public static class AttachmentStatus {

        @Schema(description = "Permalink of attachment")
        private String permalink;

    }
}
