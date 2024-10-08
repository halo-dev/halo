package run.halo.app.core.extension.content;

import static java.lang.Boolean.parseBoolean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.infra.ConditionList;

/**
 * <p>Post extension.</p>
 *
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = Post.KIND,
    plural = "posts", singular = "post")
@EqualsAndHashCode(callSuper = true)
public class Post extends AbstractExtension {

    public static final String KIND = "Post";

    public static final String REQUIRE_SYNC_ON_STARTUP_INDEX_NAME = "requireSyncOnStartup";

    public static final GroupVersionKind GVK = GroupVersionKind.fromExtension(Post.class);

    public static final String CATEGORIES_ANNO = "content.halo.run/categories";
    public static final String LAST_RELEASED_SNAPSHOT_ANNO =
        "content.halo.run/last-released-snapshot";
    public static final String LAST_ASSOCIATED_TAGS_ANNO = "content.halo.run/last-associated-tags";
    public static final String LAST_ASSOCIATED_CATEGORIES_ANNO =
        "content.halo.run/last-associated-categories";

    public static final String STATS_ANNO = "content.halo.run/stats";

    /**
     * <p>The key of the label that indicates that the post is scheduled to be published.</p>
     * <p>Can be used to query posts that are scheduled to be published.</p>
     */
    public static final String SCHEDULING_PUBLISH_LABEL = "content.halo.run/scheduling-publish";

    public static final String DELETED_LABEL = "content.halo.run/deleted";
    public static final String PUBLISHED_LABEL = "content.halo.run/published";
    public static final String OWNER_LABEL = "content.halo.run/owner";
    public static final String VISIBLE_LABEL = "content.halo.run/visible";

    public static final String ARCHIVE_YEAR_LABEL = "content.halo.run/archive-year";

    public static final String ARCHIVE_MONTH_LABEL = "content.halo.run/archive-month";
    public static final String ARCHIVE_DAY_LABEL = "content.halo.run/archive-day";

    @Schema(requiredMode = RequiredMode.REQUIRED)
    private PostSpec spec;

    @Schema
    private PostStatus status;

    @JsonIgnore
    public PostStatus getStatusOrDefault() {
        if (this.status == null) {
            this.status = new PostStatus();
        }
        return status;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return Objects.equals(true, spec.getDeleted())
            || getMetadata().getDeletionTimestamp() != null;
    }

    @JsonIgnore
    public boolean isPublished() {
        return isPublished(this.getMetadata());
    }

    public static boolean isPublished(MetadataOperator metadata) {
        var labels = metadata.getLabels();
        return labels != null && parseBoolean(labels.getOrDefault(PUBLISHED_LABEL, "false"));
    }

    public static boolean isRecycled(MetadataOperator metadata) {
        var labels = metadata.getLabels();
        return labels != null && parseBoolean(labels.getOrDefault(DELETED_LABEL, "false"));
    }

    public static boolean isPublic(PostSpec spec) {
        return spec.getVisible() == null || VisibleEnum.PUBLIC.equals(spec.getVisible());
    }

    @Data
    public static class PostSpec {
        @Schema(requiredMode = RequiredMode.REQUIRED, minLength = 1)
        private String title;

        @Schema(requiredMode = RequiredMode.REQUIRED, minLength = 1)
        private String slug;

        /**
         * 文章引用到的已发布的内容，用于主题端显示.
         */
        private String releaseSnapshot;

        private String headSnapshot;

        private String baseSnapshot;

        private String owner;

        private String template;

        private String cover;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "false")
        private Boolean deleted;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "false")
        private Boolean publish;

        private Instant publishTime;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "false")
        private Boolean pinned;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "true")
        private Boolean allowComment;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "PUBLIC")
        private VisibleEnum visible;

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "0")
        private Integer priority;

        @Schema(requiredMode = RequiredMode.REQUIRED)
        private Excerpt excerpt;

        private List<String> categories;

        private List<String> tags;

        private List<Map<String, String>> htmlMetas;
    }

    @Data
    public static class PostStatus {
        private String phase;

        @Schema
        private ConditionList conditions;

        private String permalink;

        private String excerpt;

        private Boolean inProgress;

        private Integer commentsCount;

        private List<String> contributors;

        /**
         * see {@link Category.CategorySpec#isHideFromList()}.
         */
        private Boolean hideFromList;

        private Instant lastModifyTime;

        private Long observedVersion;

        @JsonIgnore
        public ConditionList getConditionsOrDefault() {
            if (this.conditions == null) {
                this.conditions = new ConditionList();
            }
            return conditions;
        }
    }

    @Data
    public static class Excerpt {

        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "true")
        private Boolean autoGenerate;

        private String raw;
    }

    public enum PostPhase {
        DRAFT,
        PENDING_APPROVAL,
        PUBLISHED,
        FAILED;

        /**
         * Convert string value to {@link PostPhase}.
         *
         * @param value enum value string
         * @return {@link PostPhase} if found, otherwise null
         */
        public static PostPhase from(String value) {
            for (PostPhase phase : PostPhase.values()) {
                if (phase.name().equalsIgnoreCase(value)) {
                    return phase;
                }
            }
            return null;
        }
    }

    public enum VisibleEnum {
        PUBLIC,
        INTERNAL,
        PRIVATE;

        /**
         * Convert value string to {@link VisibleEnum}.
         *
         * @param value enum value string
         * @return {@link VisibleEnum} if found, otherwise null
         */
        public static VisibleEnum from(String value) {
            for (VisibleEnum visible : VisibleEnum.values()) {
                if (visible.name().equalsIgnoreCase(value)) {
                    return visible;
                }
            }
            return null;
        }
    }
}
