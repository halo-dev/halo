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
    @Schema(
            requiredMode = REQUIRED,
            description = "Desired state of the comment, including the subject, owner, body, and moderation flags.")
    private CommentSpec spec;

    /** Observed state of the comment, including reply counters and unread state. */
    @Schema(description = "Observed state of the comment, including reply counters and unread state.")
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

        /** Reference to the extension that this comment belongs to, such as a Post or SinglePage. */
        @Schema(
                requiredMode = REQUIRED,
                description = "Reference to the extension that this comment belongs to, such as a Post or SinglePage.")
        private Ref subjectRef;

        /** Latest time the comment owner has read replies for this comment. */
        @Schema(description = "Latest time the comment owner has read replies for this comment.")
        private Instant lastReadTime;
    }

    @Data
    @Schema(description = "Common desired state shared by comments and replies.")
    public static class BaseCommentSpec {

        /** Raw comment body submitted by the owner before HTML sanitization or rendering. */
        @Schema(
                requiredMode = REQUIRED,
                minLength = 1,
                description = "Raw comment body submitted by the owner before HTML sanitization or rendering.")
        private String raw;

        /** HTML content that is rendered on the theme side. */
        @Schema(
                requiredMode = REQUIRED,
                minLength = 1,
                description = "HTML content that is rendered on the theme side.")
        private String content;

        /** Identity of the comment owner. */
        @Schema(requiredMode = REQUIRED, description = "Identity of the comment owner.")
        private CommentOwner owner;

        /** Browser user agent recorded when the comment was submitted. */
        @Schema(description = "Browser user agent recorded when the comment was submitted.")
        private String userAgent;

        /** IP address recorded when the comment was submitted. */
        @Schema(description = "IP address recorded when the comment was submitted.")
        private String ipAddress;

        /** Time when the comment was approved. */
        @Schema(description = "Time when the comment was approved.")
        private Instant approvedTime;

        /** The user-defined creation time default is <code>metadata.creationTimestamp</code>. */
        @Schema(description = "User-defined creation time. Defaults to metadata.creationTimestamp when omitted.")
        private Instant creationTime;

        /** Sorting priority; larger values are ordered ahead by consumers that sort by priority. */
        @Schema(
                requiredMode = REQUIRED,
                defaultValue = "0",
                description = "Sorting priority. Larger values are ordered ahead by consumers that sort by priority.")
        private Integer priority;

        /** Whether the comment is pinned above normal comment ordering. */
        @Schema(
                requiredMode = REQUIRED,
                defaultValue = "false",
                description = "Whether the comment is pinned above normal comment ordering.")
        private Boolean top;

        /** Whether notification subscriptions should be created for this comment or reply. */
        @Schema(
                requiredMode = REQUIRED,
                defaultValue = "true",
                description = "Whether notification subscriptions should be created for this comment or reply.")
        private Boolean allowNotification;

        /** Whether the comment has passed moderation and may be visible. */
        @Schema(
                requiredMode = REQUIRED,
                defaultValue = "false",
                description = "Whether the comment has passed moderation and may be visible.")
        private Boolean approved;

        /** Whether the comment should be hidden from theme-side public output. */
        @Schema(
                requiredMode = REQUIRED,
                defaultValue = "false",
                description = "Whether the comment should be hidden from theme-side public output.")
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
        @Schema(
                requiredMode = REQUIRED,
                minLength = 1,
                description = "Owner identity kind. Built-in values are User and Email.")
        private String kind;

        /** Owner identifier, such as a username for User owners or an email address for Email owners. */
        @Schema(
                requiredMode = REQUIRED,
                maxLength = 64,
                description = "Owner identifier, such as a username for User owners or an email address for Email "
                        + "owners.")
        private String name;

        /** Display name shown for the owner. */
        @Schema(description = "Display name shown for the owner.")
        private String displayName;

        /** Additional owner metadata, such as avatar, website, or email hash. */
        @Schema(description = "Additional owner metadata, such as avatar, website, or email hash.")
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
        @Schema(description = "Creation time of the latest reply under this comment.")
        private Instant lastReplyTime;

        /** Total number of replies under this comment. */
        @Schema(description = "Total number of replies under this comment.")
        private Integer replyCount;

        /** Total number of approved and non-hidden replies under this comment. */
        @Schema(description = "Total number of approved and non-hidden replies under this comment.")
        private Integer visibleReplyCount;

        /** Number of replies created after {@link CommentSpec#lastReadTime}. */
        @Schema(description = "Number of replies created after spec.lastReadTime.")
        private Integer unreadReplyCount;

        /** Whether {@link #unreadReplyCount} is greater than zero. */
        @Schema(description = "Whether unreadReplyCount is greater than zero.")
        private Boolean hasNewReply;

        /** Metadata version observed by the last successful reconciliation. */
        @Schema(description = "Metadata version observed by the last successful reconciliation.")
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
