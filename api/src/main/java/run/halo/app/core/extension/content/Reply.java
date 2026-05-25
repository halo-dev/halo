package run.halo.app.core.extension.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Reply extension that belongs to a top-level {@link Comment}.
 *
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = Reply.KIND, plural = "replies", singular = "reply")
@EqualsAndHashCode(callSuper = true)
public class Reply extends AbstractExtension {

    public static final String KIND = "Reply";

    public static final String REQUIRE_SYNC_ON_STARTUP_INDEX_NAME = "requireSyncOnStartup";

    /** Desired state of the reply, including the parent comment, optional quoted reply, owner, and body. */
    @Schema(requiredMode = REQUIRED)
    private ReplySpec spec;

    /** Observed state of the reply. */
    @Schema(requiredMode = REQUIRED)
    private Status status = new Status();

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "Desired state of a reply.")
    public static class ReplySpec extends Comment.BaseCommentSpec {

        /** Parent Comment metadata.name. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String commentName;

        /** Quoted Reply metadata.name when this reply responds to another reply. */
        private String quoteReply;
    }

    @Data
    @Schema(name = "ReplyStatus", description = "Observed state of a reply.")
    public static class Status {
        /** Metadata version observed by the last successful reconciliation. */
        private Long observedVersion;
    }

    public void setStatus(@Nullable Status status) {
        this.status = status == null ? new Status() : status;
    }
}
