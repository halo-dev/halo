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
import org.jspecify.annotations.Nullable;
import org.springframework.util.CollectionUtils;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Ref;

/**
 * Comment extension for a post, single page, or another commentable subject.
 *
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = Comment.KIND, plural = "comments", singular = "comment")
@EqualsAndHashCode(callSuper = true)
public class Comment extends AbstractExtension {

    public static final String KIND = "Comment";

    public static final String REQUIRE_SYNC_ON_STARTUP_INDEX_NAME = "requireSyncOnStartup";

    /** Desired state of the comment, including the subject, owner, body, and moderation flags. */
    @Schema(requiredMode = REQUIRED)
    private CommentSpec spec;

    /** Observed state of the comment, including reply counters and unread state. */
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
    @Schema(description = "Desired state of a top-level comment.")
    public static class CommentSpec extends BaseCommentSpec {

        /** Reference to the comment subject extension, such as a Post or SinglePage. */
        @Schema(requiredMode = REQUIRED)
        private Ref subjectRef;

        /** Latest time the comment owner has read replies for this comment. */
        private Instant lastReadTime;
    }

    @Data
    @Schema(description = "Common desired state shared by comments and replies.")
    public static class BaseCommentSpec {

        /** Raw comment body submitted by the owner before HTML sanitization and rendering. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String raw;

        /** Rendered HTML content displayed to REST and theme consumers. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String content;

        /** Identity of the comment owner. */
        @Schema(requiredMode = REQUIRED)
        private CommentOwner owner;

        /** Browser user agent recorded when the comment was submitted. */
        private String userAgent;

        /** IP address recorded when the comment was submitted. */
        private String ipAddress;

        /** Time when the comment was approved. */
        private Instant approvedTime;

        /** Creation time supplied by the caller. If absent, metadata.creationTimestamp is used. */
        private Instant creationTime;

        /** Sorting priority. Higher values sort before lower values where priority ordering is applied. */
        @Schema(requiredMode = REQUIRED, defaultValue = "0")
        private Integer priority;

        /** Whether the comment is pinned above normal comment ordering. */
        @Schema(requiredMode = REQUIRED, defaultValue = "false")
        private Boolean top;

        /** Whether notification subscriptions should be created for this comment or reply. */
        @Schema(requiredMode = REQUIRED, defaultValue = "true")
        private Boolean allowNotification;

        /** Whether the comment has passed moderation and may be visible. */
        @Schema(requiredMode = REQUIRED, defaultValue = "false")
        private Boolean approved;

        /** Whether the comment should be hidden from public REST and theme output. */
        @Schema(requiredMode = REQUIRED, defaultValue = "false")
        private Boolean hidden;
    }

    @Data
    @Schema(description = "Identity of a comment or reply owner.")
    public static class CommentOwner {
        public static final String KIND_EMAIL = "Email";
        public static final String AVATAR_ANNO = "avatar";
        public static final String WEBSITE_ANNO = "website";
        public static final String EMAIL_HASH_ANNO = "email-hash";

        /** Owner identity kind. Built-in values are User and Email. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String kind;

        /** Owner identifier. For User owners, this is the User metadata.name; for Email owners, an email address. */
        @Schema(requiredMode = REQUIRED, maxLength = 64)
        private String name;

        /** Display name shown for the owner. */
        private String displayName;

        /** Additional owner metadata, such as avatar, website, or email hash. */
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
    @Schema(description = "Observed state of a comment.")
    public static class CommentStatus {

        /** Creation time of the latest reply under this comment. */
        private Instant lastReplyTime;

        /** Total number of replies under this comment. */
        private Integer replyCount;

        /** Total number of approved and non-hidden replies under this comment. */
        private Integer visibleReplyCount;

        /** Number of replies created after the comment owner's last read time. */
        private Integer unreadReplyCount;

        /** Whether unreadReplyCount is greater than zero. */
        private Boolean hasNewReply;

        /** Metadata version observed by the last successful reconciliation. */
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
                    Instant creationTime = defaultIfNull(
                            existingReply.getSpec().getCreationTime(),
                            existingReply.getMetadata().getCreationTimestamp());
                    return creationTime.isAfter(lastReadTime);
                })
                .count();
        return (int) unreadReplyCount;
    }
}
