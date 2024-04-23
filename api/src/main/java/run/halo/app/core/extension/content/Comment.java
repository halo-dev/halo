package run.halo.app.core.extension.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
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
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = Comment.KIND,
    plural = "comments", singular = "comment")
@EqualsAndHashCode(callSuper = true)
public class Comment extends AbstractExtension {

    public static final String KIND = "Comment";

    public static final String REQUIRE_SYNC_ON_STARTUP_INDEX_NAME = "requireSyncOnStartup";

    @Schema(requiredMode = REQUIRED)
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
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    public static class CommentSpec extends BaseCommentSpec {

        @Schema(requiredMode = REQUIRED)
        private Ref subjectRef;

        private Instant lastReadTime;
    }

    @Data
    public static class BaseCommentSpec {

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String raw;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String content;

        @Schema(requiredMode = REQUIRED)
        private CommentOwner owner;

        private String userAgent;

        private String ipAddress;

        private Instant approvedTime;

        /**
         * The user-defined creation time default is <code>metadata.creationTimestamp</code>.
         */
        private Instant creationTime;

        @Schema(requiredMode = REQUIRED, defaultValue = "0")
        private Integer priority;

        @Schema(requiredMode = REQUIRED, defaultValue = "false")
        private Boolean top;

        @Schema(requiredMode = REQUIRED, defaultValue = "true")
        private Boolean allowNotification;

        @Schema(requiredMode = REQUIRED, defaultValue = "false")
        private Boolean approved;

        @Schema(requiredMode = REQUIRED, defaultValue = "false")
        private Boolean hidden;
    }

    @Data
    public static class CommentOwner {
        public static final String KIND_EMAIL = "Email";
        public static final String AVATAR_ANNO = "avatar";
        public static final String WEBSITE_ANNO = "website";
        public static final String EMAIL_HASH_ANNO = "email-hash";

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String kind;

        @Schema(requiredMode = REQUIRED, maxLength = 64)
        private String name;

        private String displayName;

        private Map<String, String> annotations;

        @Nullable
        @JsonIgnore
        public String getAnnotation(String key) {
            return annotations == null ? null : annotations.get(key);
        }

        public static String ownerIdentity(String kind, String name) {
            return kind + "#" + name;
        }
    }

    @Data
    public static class CommentStatus {

        private Instant lastReplyTime;

        private Integer replyCount;

        private Integer visibleReplyCount;

        private Integer unreadReplyCount;

        private Boolean hasNewReply;

        private Long observedVersion;
    }

    public static String toSubjectRefKey(Ref subjectRef) {
        return subjectRef.getGroup() + "/" + subjectRef.getKind() + "/" + subjectRef.getName();
    }

    public static int getUnreadReplyCount(List<Reply> replies, Instant lastReadTime) {
        if (CollectionUtils.isEmpty(replies)) {
            return 0;
        }
        long unreadReplyCount = replies.stream()
            .filter(existingReply -> {
                if (lastReadTime == null) {
                    return true;
                }
                Instant creationTime = defaultIfNull(existingReply.getSpec().getCreationTime(),
                    existingReply.getMetadata().getCreationTimestamp());
                return creationTime.isAfter(lastReadTime);
            })
            .count();
        return (int) unreadReplyCount;
    }
}
