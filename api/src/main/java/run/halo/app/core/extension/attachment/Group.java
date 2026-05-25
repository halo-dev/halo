package run.halo.app.core.extension.attachment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static run.halo.app.core.extension.attachment.Group.KIND;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/** Attachment group extension used to organize attachments and expose aggregate counts. */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = KIND, plural = "groups", singular = "group")
public class Group extends AbstractExtension {

    public static final String KIND = "Group";
    public static final String HIDDEN_LABEL = "halo.run/hidden";

    /** Desired group metadata. */
    @Schema(requiredMode = REQUIRED)
    private GroupSpec spec;

    /** Observed group aggregate state. */
    private GroupStatus status;

    /** Desired attachment group metadata. */
    @Data
    public static class GroupSpec {

        /** Display name shown for the attachment group. */
        @Schema(requiredMode = REQUIRED)
        private String displayName;
    }

    /** Observed attachment group aggregate state. */
    @Data
    public static class GroupStatus {

        /** Last time the group aggregate state was updated. */
        private Instant updateTimestamp;

        /** Total number of attachments under the group. */
        @Schema(minimum = "0")
        private Long totalAttachments;
    }
}
