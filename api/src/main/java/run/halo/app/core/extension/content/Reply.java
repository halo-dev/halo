package run.halo.app.core.extension.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = Reply.KIND,
    plural = "replies", singular = "reply")
@EqualsAndHashCode(callSuper = true)
public class Reply extends AbstractExtension {

    public static final String KIND = "Reply";

    public static final String REQUIRE_SYNC_ON_STARTUP_INDEX_NAME = "requireSyncOnStartup";

    @Schema(requiredMode = REQUIRED)
    private ReplySpec spec;

    @Schema
    @Getter(onMethod_ = @NonNull)
    private Status status = new Status();

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ReplySpec extends Comment.BaseCommentSpec {

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String commentName;

        private String quoteReply;
    }

    @Data
    @Schema(name = "ReplyStatus")
    public static class Status {
        private Long observedVersion;
    }

    public void setStatus(Status status) {
        this.status = status == null ? new Status() : status;
    }
}
