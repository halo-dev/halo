package run.halo.app.core.extension.content;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.Instant;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;

/**
 * <p>Issue extension for user-reported issues/problems.</p>
 *
 * @author halo-copilot
 * @since 2.21.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = Issue.KIND,
    plural = "issues", singular = "issue")
@EqualsAndHashCode(callSuper = true)
public class Issue extends AbstractExtension {

    public static final String KIND = "Issue";

    public static final GroupVersionKind GVK = GroupVersionKind.fromExtension(Issue.class);

    public static final String DELETED_LABEL = "content.halo.run/deleted";
    public static final String OWNER_LABEL = "content.halo.run/owner";
    public static final String STATUS_LABEL = "content.halo.run/status";

    @Schema(requiredMode = RequiredMode.REQUIRED)
    private IssueSpec spec;

    @Schema
    private IssueStatus status;

    public boolean isDeleted() {
        return Objects.equals(true, spec.getDeleted())
            || getMetadata().getDeletionTimestamp() != null;
    }

    @Data
    public static class IssueSpec {
        @Schema(requiredMode = RequiredMode.REQUIRED, minLength = 1)
        private String title;

        @Schema(requiredMode = RequiredMode.REQUIRED)
        private String description;

        @Schema(requiredMode = RequiredMode.REQUIRED)
        private String owner;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "OPEN")
        private String status;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "LOW")
        private String priority;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "BUG")
        private String type;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "false")
        private Boolean deleted;

        private Instant resolvedTime;

        private String assignee;

        private String category;

        @Data
        public static class StatusEnum {
            public static final String OPEN = "OPEN";
            public static final String IN_PROGRESS = "IN_PROGRESS";
            public static final String RESOLVED = "RESOLVED";
            public static final String CLOSED = "CLOSED";
        }

        @Data
        public static class PriorityEnum {
            public static final String LOW = "LOW";
            public static final String MEDIUM = "MEDIUM";
            public static final String HIGH = "HIGH";
            public static final String CRITICAL = "CRITICAL";
        }

        @Data
        public static class TypeEnum {
            public static final String BUG = "BUG";
            public static final String FEATURE_REQUEST = "FEATURE_REQUEST";
            public static final String IMPROVEMENT = "IMPROVEMENT";
            public static final String QUESTION = "QUESTION";
        }
    }

    @Data
    public static class IssueStatus {
        private String phase;
        private String observedVersion;
        private Instant lastModifyTime;
    }
}