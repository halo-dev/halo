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

/**
 * Tag extension for grouping posts by free-form labels.
 *
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@Schema(description = "Tag extension for grouping posts by free-form labels.")
@ToString(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = Tag.KIND, plural = "tags", singular = "tag")
@EqualsAndHashCode(callSuper = true)
public class Tag extends AbstractExtension {

    public static final String KIND = "Tag";

    public static final GroupVersionKind GVK = GroupVersionKind.fromExtension(Tag.class);

    public static final String REQUIRE_SYNC_ON_STARTUP_INDEX_NAME = "requireSyncOnStartup";

    /** Desired state of the tag, including display information and visual hints. */
    @Schema(
            requiredMode = REQUIRED,
            description = "Desired state of the tag, including display information and visual hints.")
    @Nullable
    private TagSpec spec;

    /** Observed state of the tag, including permalink and post counters. */
    @Schema(description = "Observed state of the tag, including permalink and post counters.")
    @Nullable
    private TagStatus status;

    @Data
    @Schema(description = "Desired display and rendering configuration of a tag.")
    public static class TagSpec {

        /** Display name of the tag. */
        @Schema(requiredMode = REQUIRED, minLength = 1, description = "Display name of the tag.")
        private String displayName;

        /** URL slug used to build the tag permalink. */
        @Schema(requiredMode = REQUIRED, minLength = 1, description = "URL slug of the tag.")
        private String slug;

        /** Human-readable description of the tag. */
        @Schema(description = "Human-readable description of the tag.")
        private String description;

        /**
         * Color regex explanation.
         *
         * <pre>
         * ^                 # start of the line
         * #                 # start with a number sign `#`
         * (                 # start of (group 1)
         *   [a-fA-F0-9]{6}  # support a-f, A-F and 0-9, with a length of 6
         *   |               # or
         *   [a-fA-F0-9]{3}  # support a-f, A-F and 0-9, with a length of 3
         * )                 # end of (group 1)
         * $                 # end of the line
         * </pre>
         */
        @Schema(
                pattern = "^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$",
                description = "Display color of the tag in 3- or 6-digit hex notation.")
        private String color;

        /** Cover image URL or attachment URI of the tag. */
        @Schema(description = "Cover image URL or attachment URI of the tag.")
        private String cover;
    }

    @JsonIgnore
    public TagStatus getStatusOrDefault() {
        if (this.status == null) {
            this.status = new TagStatus();
        }
        return this.status;
    }

    @Data
    @Schema(description = "Observed state of a tag.")
    public static class TagStatus {

        /** Absolute permalink calculated from the tag permalink policy. */
        @Schema(description = "Absolute permalink calculated from the tag permalink policy.")
        private String permalink;

        /** Total number of published and public posts associated with the tag. */
        @Schema(description = "Total number of published and public posts associated with the tag.")
        public Integer visiblePostCount;

        /** Total number of posts associated with the tag. */
        @Schema(description = "Total number of posts associated with the tag.")
        public Integer postCount;

        /** Metadata version observed by the last successful reconciliation. */
        @Schema(description = "Metadata version observed by the last successful reconciliation.")
        private Long observedVersion;
    }
}
