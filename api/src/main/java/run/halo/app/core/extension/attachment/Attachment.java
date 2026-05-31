package run.halo.app.core.extension.attachment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static run.halo.app.core.extension.attachment.Attachment.KIND;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/** Attachment extension that describes an uploaded file and its resolved access URLs. */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = KIND, plural = "attachments", singular = "attachment")
public class Attachment extends AbstractExtension {

    public static final String KIND = "Attachment";

    /** Desired attachment metadata, storage references, and classification fields. */
    @Schema(requiredMode = REQUIRED)
    private AttachmentSpec spec;

    /** Observed attachment access URLs and generated thumbnails. */
    private AttachmentStatus status;

    /** Desired attachment metadata and storage placement. */
    @Data
    public static class AttachmentSpec {

        /** Display name shown for the attachment. */
        private String displayName;

        /** Attachment group metadata.name this attachment belongs to. */
        private String groupName;

        /** Storage policy metadata.name used to store this attachment. */
        private String policyName;

        /** User metadata.name of the uploader. */
        private String ownerName;

        /** Media type detected or supplied for the attachment, such as image/png. */
        @Nullable
        private String mediaType;

        /** Size of the attachment in bytes. */
        @Schema(minimum = "0")
        private Long size;

        /** Free-form tags assigned to the attachment. */
        private Set<String> tags;
    }

    /** Observed attachment access state. */
    @Data
    public static class AttachmentStatus {

        /**
         * Public permalink for the attachment. Local storage usually exposes a public URL, while object storage may
         * expose the object URL.
         */
        private String permalink;

        /** Generated thumbnail URLs keyed by thumbnail size name. */
        @Nullable
        private Map<String, String> thumbnails;
    }
}
