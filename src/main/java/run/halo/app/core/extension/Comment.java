package run.halo.app.core.extension;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.lang.Nullable;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Ref;

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

    @Schema
    private CommentStatus status;

    @JsonIgnore
    public CommentStatus getStatusOrDefault() {
        if (this.status == null) {
            this.status = new CommentStatus();
        }
        return this.status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CommentSpec extends BaseCommentSpec {

        @Schema(required = true)
        private Ref subjectRef;

        private Instant lastReadTime;
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
        public static final String KIND_EMAIL = "Email";
        public static final String AVATAR_ANNO = "avatar";
        public static final String WEBSITE_ANNO = "website";

        @Schema(required = true, minLength = 1)
        private String kind;

        @Schema(required = true, maxLength = 64)
        private String name;

        private String displayName;

        private Map<String, String> annotations;

        @Nullable
        @JsonIgnore
        public String getAnnotation(String key) {
            return annotations == null ? null : annotations.get(key);
        }
    }

    @Data
    public static class CommentStatus {

        private Instant lastReplyTime;

        private Integer replyCount;

        private Integer unreadReplyCount;

        public boolean getHasNewReply() {
            return unreadReplyCount != null && unreadReplyCount > 0;
        }
    }
}
