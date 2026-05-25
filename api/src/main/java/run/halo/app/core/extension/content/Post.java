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
import org.jspecify.annotations.Nullable;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.infra.ConditionList;

/**
 * Post extension that stores article metadata, publication settings, taxonomy, and derived status.
 *
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = Post.KIND, plural = "posts", singular = "post")
@EqualsAndHashCode(callSuper = true)
public class Post extends AbstractExtension {

    public static final String KIND = "Post";

    public static final String REQUIRE_SYNC_ON_STARTUP_INDEX_NAME = "requireSyncOnStartup";

    public static final GroupVersionKind GVK = GroupVersionKind.fromExtension(Post.class);

    public static final String CATEGORIES_ANNO = "content.halo.run/categories";
    public static final String LAST_RELEASED_SNAPSHOT_ANNO = "content.halo.run/last-released-snapshot";
    public static final String LAST_ASSOCIATED_TAGS_ANNO = "content.halo.run/last-associated-tags";
    public static final String LAST_ASSOCIATED_CATEGORIES_ANNO = "content.halo.run/last-associated-categories";

    public static final String STATS_ANNO = "content.halo.run/stats";

    /**
     * The key of the label that indicates that the post is scheduled to be published.
     *
     * <p>Can be used to query posts that are scheduled to be published.
     */
    public static final String SCHEDULING_PUBLISH_LABEL = "content.halo.run/scheduling-publish";

    public static final String DELETED_LABEL = "content.halo.run/deleted";
    public static final String PUBLISHED_LABEL = "content.halo.run/published";
    public static final String OWNER_LABEL = "content.halo.run/owner";
    public static final String VISIBLE_LABEL = "content.halo.run/visible";

    public static final String ARCHIVE_YEAR_LABEL = "content.halo.run/archive-year";

    public static final String ARCHIVE_MONTH_LABEL = "content.halo.run/archive-month";
    public static final String ARCHIVE_DAY_LABEL = "content.halo.run/archive-day";

    /** Desired state of the post, including content snapshots, publishing options, taxonomy, and theme hints. */
    @Schema(requiredMode = RequiredMode.REQUIRED)
    @Nullable
    private PostSpec spec;

    /** Observed state of the post, populated by reconcilers and other internal controllers. */
    @Nullable
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
        return Objects.equals(true, spec.getDeleted()) || getMetadata().getDeletionTimestamp() != null;
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

    /** Desired post content, publication, taxonomy, and rendering configuration. */
    @Data
    public static class PostSpec {
        /** Display title of the post. */
        @Schema(requiredMode = RequiredMode.REQUIRED, minLength = 1)
        private String title;

        /** URL slug used to build the post permalink. */
        @Schema(requiredMode = RequiredMode.REQUIRED, minLength = 1)
        private String slug;

        /** Snapshot metadata.name selected as the published content version. */
        private String releaseSnapshot;

        /** Snapshot metadata.name containing the latest editable draft content. */
        private String headSnapshot;

        /** Base Snapshot metadata.name used to reconstruct raw and rendered content from patches. */
        private String baseSnapshot;

        /** User metadata.name of the post owner. */
        private String owner;

        /** Theme template used to render this post. */
        private String template;

        /** Cover image URL or attachment URI of the post. */
        private String cover;

        /** Whether the post is logically deleted and should be treated as recycled. */
        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "false")
        private Boolean deleted;

        /** Desired publish state. False keeps the post as a draft or moves it back to draft. */
        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "false")
        private Boolean publish;

        /** Time when the post was published or is scheduled to be published. */
        private Instant publishTime;

        /** Whether the post should be pinned ahead of normal post ordering. */
        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "false")
        private Boolean pinned;

        /** Whether new comments are allowed for this post. */
        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "true")
        private Boolean allowComment;

        /** Visibility used by theme-side and public REST queries; anonymous clients only receive PUBLIC posts. */
        @Schema(requiredMode = RequiredMode.REQUIRED)
        private VisibleEnum visible;

        /** Sorting priority. Higher values sort before lower values where priority ordering is applied. */
        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "0")
        private Integer priority;

        /** Excerpt configuration for the post. */
        @Schema(requiredMode = RequiredMode.REQUIRED)
        private Excerpt excerpt;

        /** Category metadata.name values associated with the post. */
        private List<String> categories;

        /** Tag metadata.name values associated with the post. */
        private List<String> tags;

        /** HTML meta tag attribute maps injected into the rendered post page head. */
        private List<Map<String, String>> htmlMetas;
    }

    /** Observed post state derived by content reconcilers. */
    @Data
    public static class PostStatus {
        /** Current publishing phase, such as DRAFT, PENDING_APPROVAL, PUBLISHED, or FAILED. */
        private String phase;

        /** Reconciliation conditions reported by controllers for this content item. */
        private ConditionList conditions;

        /** Absolute permalink calculated from the content permalink policy. */
        private String permalink;

        /** Excerpt text resolved from the content excerpt configuration. */
        private String excerpt;

        /** Whether the latest draft snapshot differs from the released snapshot. */
        private Boolean inProgress;

        /** Total number of comments associated with the content item. */
        private Integer commentsCount;

        /** User metadata.name values that contributed to the content snapshots. */
        private List<String> contributors;

        /** Whether any associated category hides this content item from theme-side lists. */
        private Boolean hideFromList;

        /** Last modification time of the released snapshot. */
        private Instant lastModifyTime;

        /** Metadata version observed by the last successful reconciliation. */
        private Long observedVersion;

        @JsonIgnore
        public ConditionList getConditionsOrDefault() {
            if (this.conditions == null) {
                this.conditions = new ConditionList();
            }
            return conditions;
        }
    }

    /** Excerpt generation configuration for a post or single page. */
    @Data
    public static class Excerpt {

        /** Whether Halo should generate the excerpt from the released content automatically. */
        @Schema(requiredMode = RequiredMode.REQUIRED, defaultValue = "true")
        private Boolean autoGenerate;

        /** Manual excerpt text used when autoGenerate is false. */
        private String raw;
    }

    /** Publishing phase calculated for a post. */
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

    /** Visibility options used by theme-side content queries. */
    public enum VisibleEnum {
        /** Eligible for public theme-side listings and routing. */
        PUBLIC,
        /** Non-public visibility value for internal or caller-specific access rules. */
        INTERNAL,
        /** Non-public visibility value intended for owner-only content. */
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
