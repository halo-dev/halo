package run.halo.app.core.extension.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static run.halo.app.core.extension.content.Category.KIND;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;

/**
 * Category extension for grouping posts and building category archives.
 *
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@Schema(description = "Category extension for grouping posts and building category archives.")
@ToString(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = KIND, plural = "categories", singular = "category")
@EqualsAndHashCode(callSuper = true)
public class Category extends AbstractExtension {

    public static final String KIND = "Category";
    public static final String LAST_HIDDEN_STATE_ANNO = "content.halo.run/last-hidden-state";

    public static final GroupVersionKind GVK = GroupVersionKind.fromExtension(Category.class);

    /** Desired state of the category, including display information, hierarchy, and theme templates. */
    @Schema(
            requiredMode = REQUIRED,
            description = "Desired state of the category, including display information, hierarchy, and theme "
                    + "templates.")
    @Nullable
    private CategorySpec spec;

    /** Observed state of the category, including permalink and post counters. */
    @Schema(description = "Observed state of the category, including permalink and post counters.")
    @Nullable
    private CategoryStatus status;

    @JsonIgnore
    public boolean isDeleted() {
        return getMetadata().getDeletionTimestamp() != null;
    }

    @Data
    @Schema(description = "Desired display, hierarchy, and rendering configuration of a category.")
    public static class CategorySpec {

        /** Display name of the category. */
        @Schema(requiredMode = REQUIRED, minLength = 1, description = "Display name of the category.")
        private String displayName;

        /** URL slug used to build the category permalink. */
        @Schema(requiredMode = REQUIRED, minLength = 1, description = "URL slug of the category.")
        private String slug;

        /** Human-readable description of the category. */
        @Schema(description = "Human-readable description of the category.")
        private String description;

        /** Cover image URL or attachment URI of the category. */
        @Schema(description = "Cover image URL or attachment URI of the category.")
        private String cover;

        /** Theme template used to render the category archive page. */
        @Schema(
                requiredMode = NOT_REQUIRED,
                maxLength = 255,
                description = "Theme template used to render the category archive page.")
        private String template;

        /**
         * Used to specify the template for the posts associated with the category.
         *
         * <p>The priority is not as high as that of the post.
         *
         * <p>If the post also specifies a template, the post's template will prevail.
         */
        @Schema(
                requiredMode = NOT_REQUIRED,
                maxLength = 255,
                description = "Theme template used to render posts associated with this category when the post does "
                        + "not specify its own template.")
        private String postTemplate;

        /** Sorting priority; larger values are ordered ahead by consumers that sort by priority. */
        @Schema(
                requiredMode = REQUIRED,
                defaultValue = "0",
                description = "Sorting priority. Larger values are ordered ahead by consumers that sort by priority.")
        private Integer priority;

        /** Child category extension names. */
        @Schema(description = "Child category extension names.")
        private @Nullable List<String> children;

        /**
         * if a category is queried for related posts, the default behavior is to query all posts under the category
         * including its subcategories, but if this field is set to true, cascade query behavior will be terminated
         * here.
         *
         * <p>For example, if a category has subcategories A and B, and A has subcategories C and D and C marked this
         * field as true, when querying posts under A category,all posts under A and B will be queried, but C and D will
         * not be queried.
         */
        @Schema(
                description = "Whether ancestor category post queries should stop cascading into this category's "
                        + "children.")
        private boolean preventParentPostCascadeQuery;

        /**
         * Whether to hide the category from the category list.
         *
         * <p>When set to true, the category including its subcategories and related posts will not be displayed in the
         * category list, but it can still be accessed by permalink.
         *
         * <p>Limitation: It only takes effect on the theme-side categorized list and it only allows to be set to true
         * on the first level(root node) of categories.
         */
        @Schema(description = "Whether this category tree and its posts should be hidden from theme-side lists.")
        private boolean hideFromList;
    }

    @JsonIgnore
    public CategoryStatus getStatusOrDefault() {
        if (this.status == null) {
            this.status = new CategoryStatus();
        }
        return this.status;
    }

    @Data
    @Schema(description = "Observed state of a category.")
    public static class CategoryStatus {

        /** Absolute permalink calculated from the category permalink policy. */
        @Schema(description = "Absolute permalink calculated from the category permalink policy.")
        private String permalink;

        /** Total number of posts in this category and all descendant categories. */
        @Schema(description = "Total number of posts in this category and all descendant categories.")
        public Integer postCount;

        /** Total number of published and public posts in this category and all descendant categories. */
        @Schema(
                description = "Total number of published and public posts in this category and all descendant "
                        + "categories.")
        public Integer visiblePostCount;
    }
}
