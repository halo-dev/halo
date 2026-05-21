package run.halo.app.core.extension.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.MetadataUtil;

/**
 * Single page extension.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(
        group = Constant.GROUP,
        version = Constant.VERSION,
        kind = SinglePage.KIND,
        plural = "singlepages",
        singular = "singlepage")
@EqualsAndHashCode(callSuper = true)
public class SinglePage extends AbstractExtension {

    public static final String KIND = "SinglePage";

    public static final GroupVersionKind GVK = GroupVersionKind.fromExtension(SinglePage.class);
    public static final String DELETED_LABEL = "content.halo.run/deleted";
    public static final String PUBLISHED_LABEL = "content.halo.run/published";
    public static final String LAST_RELEASED_SNAPSHOT_ANNO = "content.halo.run/last-released-snapshot";
    public static final String OWNER_LABEL = "content.halo.run/owner";
    public static final String VISIBLE_LABEL = "content.halo.run/visible";

    /** Desired state of the single page, including content snapshots, publishing options, and theme hints. */
    @Schema(
            requiredMode = REQUIRED,
            description = "Desired state of the single page, including content snapshots, publishing options, and "
                    + "theme hints.")
    @Nullable
    private SinglePageSpec spec;

    /** Observed state of the single page, populated by reconcilers and other internal controllers. */
    @Schema(description = "Observed state of the single page, populated by reconcilers and other internal controllers.")
    @Nullable
    private SinglePageStatus status;

    @JsonIgnore
    public SinglePageStatus getStatusOrDefault() {
        if (this.status == null) {
            this.status = new SinglePageStatus();
        }
        return this.status;
    }

    @JsonIgnore
    public boolean isPublished() {
        Map<String, String> labels = getMetadata().getLabels();
        return labels != null && labels.getOrDefault(PUBLISHED_LABEL, "false").equals("true");
    }

    @Data
    @Schema(description = "Desired content, publication, and rendering configuration of a single page.")
    public static class SinglePageSpec {
        /** Display title of the single page. */
        @Schema(requiredMode = REQUIRED, minLength = 1, description = "Display title of the single page.")
        private String title;

        /** URL slug used to build the single page permalink. */
        @Schema(requiredMode = REQUIRED, minLength = 1, description = "URL slug of the single page.")
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

        /** Username of the single page owner. */
        @Schema(description = "Username of the single page owner.")
        private String owner;

        /** Theme template used to render this single page. */
        @Schema(description = "Theme template used to render this single page.")
        private String template;

        /** Cover image URL or attachment URI of the single page. */
        @Schema(description = "Cover image URL or attachment URI of the single page.")
        private String cover;

        /** Whether the single page is logically deleted and should be treated as recycled. */
        @Schema(
                requiredMode = REQUIRED,
                defaultValue = "false",
                description = "Whether the single page is logically deleted and should be treated as recycled.")
        private Boolean deleted;

        /** Desired publish state; false keeps or moves the single page to draft. */
        @Schema(
                requiredMode = REQUIRED,
                defaultValue = "false",
                description = "Desired publish state. False keeps or moves the single page to draft.")
        private Boolean publish;

        /** Time when the single page was published or is scheduled to be published. */
        @Schema(description = "Time when the single page was published or is scheduled to be published.")
        private Instant publishTime;

        /** Whether the single page should be pinned ahead of normal page ordering. */
        @Schema(
                requiredMode = REQUIRED,
                defaultValue = "false",
                description = "Whether the single page should be pinned ahead of normal page ordering.")
        private Boolean pinned;

        /** Whether new comments are allowed for this single page. */
        @Schema(
                requiredMode = REQUIRED,
                defaultValue = "true",
                description = "Whether new comments are allowed for this single page.")
        private Boolean allowComment;

        /**
         * Visibility of the single page in theme-side queries; only PUBLIC content is returned to anonymous visitors.
         */
        @Schema(
                requiredMode = REQUIRED,
                description = "Visibility of the single page in theme-side queries. Only PUBLIC content is returned "
                        + "to anonymous visitors.")
        private Post.VisibleEnum visible;

        /** Sorting priority; larger values are ordered ahead by consumers that sort by priority. */
        @Schema(
                requiredMode = REQUIRED,
                defaultValue = "0",
                description = "Sorting priority. Larger values are ordered ahead by consumers that sort by priority.")
        private Integer priority;

        /** Excerpt configuration for the single page. */
        @Schema(requiredMode = REQUIRED, description = "Excerpt configuration for the single page.")
        private Post.Excerpt excerpt;

        /** HTML meta tag attribute maps injected into the single page template head. */
        @Schema(description = "HTML meta tag attribute maps injected into the single page template head.")
        private List<Map<String, String>> htmlMetas;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "Observed state of a single page.")
    public static class SinglePageStatus extends Post.PostStatus {}

    public static void changePublishedState(SinglePage page, boolean value) {
        Map<String, String> labels = MetadataUtil.nullSafeLabels(page);
        labels.put(PUBLISHED_LABEL, String.valueOf(value));
    }
}
