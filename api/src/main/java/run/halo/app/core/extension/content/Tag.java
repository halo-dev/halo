package run.halo.app.core.extension.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.localization.Localizable;
import run.halo.app.localization.LocalizableLink;
import run.halo.app.localization.LocalizableNested;
import run.halo.app.localization.LocalizableResource;
import run.halo.app.localization.LocalizableSchema;
import run.halo.app.localization.LocalizableSearchTitle;

/**
 * Tag extension for grouping posts by free-form labels.
 *
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@Schema(name = "Tag")
@ToString(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = Tag.KIND, plural = "tags", singular = "tag")
@EqualsAndHashCode(callSuper = true)
@LocalizableResource(label = "core.post_tag.title")
public class Tag extends AbstractExtension {

    public static final String KIND = "Tag";

    public static final GroupVersionKind GVK = GroupVersionKind.fromExtension(Tag.class);

    public static final String REQUIRE_SYNC_ON_STARTUP_INDEX_NAME = "requireSyncOnStartup";

    /** Desired state of the tag, including display information and visual hints. */
    @Schema(requiredMode = REQUIRED)
    @Nullable
    @LocalizableNested
    private TagSpec spec;

    /** Observed state of the tag, including permalink and post counters. */
    @Nullable
    @LocalizableNested
    private TagStatus status;

    /** Desired tag display and rendering configuration. */
    @Data
    public static class TagSpec {

        /** Display name of the tag. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        @LocalizableSearchTitle
        @Localizable(key = "displayName", label = "core.localization.field.displayName", required = true)
        private String displayName;

        /** URL slug used to build the tag permalink. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String slug;

        /** Human-readable description of the tag. */
        @Localizable(key = "description", label = "core.localization.field.description", multiline = true)
        private String description;

        /** Display color of the tag in 3- or 6-digit hex notation, such as #fff or #ffffff. */
        @Schema(pattern = "^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$")
        private String color;

        /** Cover image URL or attachment URI of the tag. */
        @Localizable(key = "cover", label = "core.localization.field.cover", type = LocalizableSchema.Type.IMAGE)
        private String cover;
    }

    @JsonIgnore
    public TagStatus getStatusOrDefault() {
        if (this.status == null) {
            this.status = new TagStatus();
        }
        return this.status;
    }

    /** Observed tag state derived by content reconcilers. */
    @Data
    public static class TagStatus {

        /** Absolute permalink calculated from the tag permalink policy. */
        @LocalizableLink
        private String permalink;

        /** Total number of published and public posts associated with the tag. */
        public Integer visiblePostCount;

        /** Total number of posts associated with the tag. */
        public Integer postCount;

        /** Metadata version observed by the last successful reconciliation. */
        private Long observedVersion;
    }
}
