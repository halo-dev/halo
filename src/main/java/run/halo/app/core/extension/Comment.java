package run.halo.app.core.extension;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = "content.halo.run", version = "v1alpha1",
    kind = Comment.KIND, plural = "comments", singular = "comment")
@EqualsAndHashCode(callSuper = true)
public class Comment extends AbstractExtension {
    public static final String KIND = "Comment";

    @Schema(required = true)
    private CommentSpec spec;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CommentSpec extends BaseCommentSpec {

        @Schema(required = true)
        private CommentSubjectRef subjectRef;
    }

    @Data
    public static class BaseCommentSpec {

        @Schema(required = true, minLength = 1)
        private String raw;

        @Schema(required = true, minLength = 1)
        private String content;

        @Schema(required = true)
        private CommentOwner owner;

        private String userAgent;

        private String ipAddress;

        @Schema(required = true, defaultValue = "0")
        private Integer priority;

        @Schema(required = true, defaultValue = "false")
        private Boolean top;

        @Schema(required = true, defaultValue = "true")
        private Boolean allowNotification;

        @Schema(required = true, defaultValue = "false")
        private Boolean approved;

        @Schema(required = true, defaultValue = "false")
        private Boolean hidden;
    }

    @Data
    public static class CommentOwner {
        @Schema(required = true, minLength = 1)
        private String kind;

        @Schema(required = true, minLength = 1, maxLength = 64)
        private String name;

        private String displayName;

        private Map<String, String> annotations;
    }

    @Data
    public static class CommentSubjectRef {
        @Schema(required = true, minLength = 1)
        private String kind;

        @Schema(required = true, minLength = 1, maxLength = 64)
        private String name;
    }
}
