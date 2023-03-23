package run.halo.app.core.extension.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;

/**
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION,
    kind = Tag.KIND, plural = "tags", singular = "tag")
@EqualsAndHashCode(callSuper = true)
public class Tag extends AbstractExtension {

    public static final String KIND = "Tag";

    public static final GroupVersionKind GVK = GroupVersionKind.fromExtension(Tag.class);

    @Schema(required = true)
    private TagSpec spec;

    @Schema
    private TagStatus status;

    @Data
    public static class TagSpec {

        @Schema(required = true, minLength = 1)
        private String displayName;

        @Schema(required = true, minLength = 1)
        private String slug;

        /**
         * Color regex explanation.
         * <pre>
         * ^                 # start of the line
         * #                 # start with a number sign `#`
         * (                 # start of (group 1)
         *   [a-fA-F0-9]{6}  # support z-f, A-F and 0-9, with a length of 6
         *   |               # or
         *   [a-fA-F0-9]{3}  # support z-f, A-F and 0-9, with a length of 3
         * )                 # end of (group 1)
         * $                 # end of the line
         * </pre>
         */
        @Schema(pattern = "^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$")
        private String color;

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
    public static class TagStatus {

        private String permalink;

        public Integer visiblePostCount;

        public Integer postCount;
    }
}
