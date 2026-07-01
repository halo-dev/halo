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
@Schema(name = "Category")
@ToString(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = KIND, plural = "categories", singular = "category")
@EqualsAndHashCode(callSuper = true)
public class Category extends AbstractExtension {

    public static final String KIND = "Category";
    public static final String LAST_HIDDEN_STATE_ANNO = "content.halo.run/last-hidden-state";
    public static final String HIERARCHY_MIGRATED_LABEL = "content.halo.run/category-hierarchy-migrated";

    public static final GroupVersionKind GVK = GroupVersionKind.fromExtension(Category.class);

    /** Desired state of the category, including display information, hierarchy, and theme templates. */
    @Schema(requiredMode = REQUIRED)
    @Nullable
    private CategorySpec spec;

    /** Observed state of the category, including permalink and post counters. */
    @Nullable
    private CategoryStatus status;

    @JsonIgnore
    public boolean isDeleted() {
        return getMetadata().getDeletionTimestamp() != null;
    }

    /** Desired category display, hierarchy, and rendering configuration. */
    @Data
    public static class CategorySpec {

        /** Display name of the category. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String displayName;

        /** URL slug used to build the category permalink. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String slug;

        /** Human-readable description of the category. */
        private String description;

        /** Cover image URL or attachment URI of the category. */
        private String cover;

        /** Theme template used to render the category archive page. */
        @Schema(requiredMode = NOT_REQUIRED, maxLength = 255)
        private String template;

        /** Theme template used for posts in this category when the post does not specify its own spec.template. */
        @Schema(requiredMode = NOT_REQUIRED, maxLength = 255)
        private String postTemplate;

        /** Sorting priority. Higher values sort before lower values where priority ordering is applied. */
        @Schema(requiredMode = REQUIRED, defaultValue = "0")
        private Integer priority;

        /** Parent Category metadata.name. Root categories leave this unset. */
        @Schema(description = "Parent Category metadata.name. Root categories leave this unset.")
        private @Nullable String parent;

        /**
         * Child Category metadata.name values.
         *
         * @deprecated since 2.26.0, use {@link #parent} instead.
         */
        @SuppressWarnings("java:S1133")
        @Deprecated(since = "2.26.0")
        @Schema(
                deprecated = true,
                description = "Legacy child Category metadata.name values. Category hierarchy is now sourced from "
                        + "Category.spec.parent.")
        private @Nullable List<String> children;

        /**
         * Stops parent category post queries from cascading into this category tree. Direct post queries for this
         * category are unaffected.
         */
        private boolean preventParentPostCascadeQuery;

        /**
         * Hides this root category tree and its posts from theme-side lists. Permalinks remain accessible, and only
         * root categories are expected to set this flag.
         */
        private boolean hideFromList;
    }

    @JsonIgnore
    public CategoryStatus getStatusOrDefault() {
        if (this.status == null) {
            this.status = new CategoryStatus();
        }
        return this.status;
    }

    /** Observed category state derived by content reconcilers. */
    @Data
    public static class CategoryStatus {

        /** Absolute permalink calculated from the category permalink policy. */
        private String permalink;

        /** Total number of posts in this category and all descendant categories. */
        public Integer postCount;

        /** Total number of published and public posts in this category and all descendant categories. */
        public Integer visiblePostCount;
    }
}
