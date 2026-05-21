package run.halo.app.core.extension.content;

import static java.lang.Boolean.parseBoolean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
 * Post extension.
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
    @Schema(
            requiredMode = RequiredMode.REQUIRED,
            description = "Desired state of the post, including content snapshots, publishing options, taxonomy, and "
                    + "theme hints.")
    @Nullable
    private PostSpec spec;

    /** Observed state of the post, populated by reconcilers and other internal controllers. */
    @Schema(description = "Observed state of the post, populated by reconcilers and other internal controllers.")
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

    @Data
    @Schema(description = "Desired content, publication, taxonomy, and rendering configuration of a post.")
    public static class PostSpec {
        /** Display title of the post. */
        @Schema(requiredMode = RequiredMode.REQUIRED, minLength = 1, description = "Display title of the post.")
        private String title;

        /** URL slug used to build the post permalink. */
        @Schema(requiredMode = RequiredMode.REQUIRED, minLength = 1, description = "URL slug of the post.")
        private String slug;

        /** Snapshot name selected as the released version for theme-side rendering after publishing. */
        @Schema(
                description =
                        "Snapshot name selected as the released version for theme-side rendering after publishing.")
        private String releaseSnapshot;

        /** Snapshot name of the latest draft content. */
        @Schema(description = "Snapshot name of the latest draft content.")
        private String headSnapshot;

        /** Base snapshot name used to apply snapshot patches. */
        @Schema(description = "Base snapshot name used to apply snapshot patches.")
        private String baseSnapshot;

        /** Username of the post owner. */
        @Schema(description = "Username of the post owner.")
        private String owner;

        /** Theme template used to render this post. */
        @Schema(description = "Theme template used to render this post.")
        private String template;

        /** Cover image URL or attachment URI of the post. */
        @Schema(description = "Cover image URL or attachment URI of the post.")
        private String cover;

        /** Whether the post is logically deleted and should be treated as recycled. */
        @Schema(
                requiredMode = RequiredMode.REQUIRED,
                defaultValue = "false",
                description = "Whether the post is logically deleted and should be treated as recycled.")
        private Boolean deleted;

        /** Desired publish state; false keeps or moves the post to draft. */
        @Schema(
                requiredMode = RequiredMode.REQUIRED,
                defaultValue = "false",
                description = "Desired publish state. False keeps or moves the post to draft.")
        private Boolean publish;

        /** Time when the post was published or is scheduled to be published. */
        @Schema(description = "Time when the post was published or is scheduled to be published.")
        private Instant publishTime;

        /** Whether the post should be pinned ahead of normal post ordering. */
        @Schema(
                requiredMode = RequiredMode.REQUIRED,
                defaultValue = "false",
                description = "Whether the post should be pinned ahead of normal post ordering.")
        private Boolean pinned;

        /** Whether new comments are allowed for this post. */
        @Schema(
                requiredMode = RequiredMode.REQUIRED,
                defaultValue = "true",
                description = "Whether new comments are allowed for this post.")
        private Boolean allowComment;

        /** Visibility of the post in theme-side queries; only PUBLIC content is returned to anonymous visitors. */
        @Schema(
                requiredMode = RequiredMode.REQUIRED,
                description = "Visibility of the post in theme-side queries. Only PUBLIC content is returned to "
                        + "anonymous visitors.")
        private VisibleEnum visible;

        /** Sorting priority; larger values are ordered ahead by consumers that sort by priority. */
        @Schema(
                requiredMode = RequiredMode.REQUIRED,
                defaultValue = "0",
                description = "Sorting priority. Larger values are ordered ahead by consumers that sort by priority.")
        private Integer priority;

        /** Excerpt configuration for the post. */
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "Excerpt configuration for the post.")
        private Excerpt excerpt;

        /** Category extension names associated with the post. */
        @Schema(description = "Category extension names associated with the post.")
        private List<String> categories;

        /** Tag extension names associated with the post. */
        @Schema(description = "Tag extension names associated with the post.")
        private List<String> tags;

        /** HTML meta tag attribute maps injected into the post template head. */
        @Schema(description = "HTML meta tag attribute maps injected into the post template head.")
        private List<Map<String, String>> htmlMetas;
    }

    @Data
    @Schema(description = "Observed state of a content item.")
    public static class PostStatus {
        /** Current publishing phase of the content item. */
        @Schema(
                description = "Current publishing phase of the content item, such as DRAFT, PENDING_APPROVAL, "
                        + "PUBLISHED, or FAILED.")
        private String phase;

        /** Latest reconciliation conditions for the content item. */
        @ArraySchema(arraySchema = @Schema(description = "Latest reconciliation conditions for the content item."))
        private ConditionList conditions;

        /** Absolute permalink calculated from the content permalink policy. */
        @Schema(description = "Absolute permalink calculated from the content permalink policy.")
        private String permalink;

        /** Excerpt text resolved from the content excerpt configuration. */
        @Schema(description = "Excerpt text resolved from the content excerpt configuration.")
        private String excerpt;

        /** Whether the latest draft snapshot differs from the released snapshot. */
        @Schema(description = "Whether the latest draft snapshot differs from the released snapshot.")
        private Boolean inProgress;

        /** Total number of comments associated with the content item. */
        @Schema(description = "Total number of comments associated with the content item.")
        private Integer commentsCount;

        /** Usernames that contributed to the content snapshots. */
        @Schema(description = "Usernames that contributed to the content snapshots.")
        private List<String> contributors;

        /** Mirrors {@link Category.CategorySpec#isHideFromList()} for any associated category. */
        @Schema(description = "Whether any associated category hides the content item from theme-side lists.")
        private Boolean hideFromList;

        /** Last modification time of the released snapshot. */
        @Schema(description = "Last modification time of the released snapshot.")
        private Instant lastModifyTime;

        /** Metadata version observed by the last successful reconciliation. */
        @Schema(description = "Metadata version observed by the last successful reconciliation.")
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
    @Schema(description = "Excerpt generation configuration.")
    public static class Excerpt {

        /** Whether Halo should generate the excerpt from the released content automatically. */
        @Schema(
                requiredMode = RequiredMode.REQUIRED,
                defaultValue = "true",
                description = "Whether Halo should generate the excerpt from the released content automatically.")
        private Boolean autoGenerate;

        /** Manual excerpt text used when auto generation is disabled. */
        @Schema(description = "Manual excerpt text used when auto generation is disabled.")
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
