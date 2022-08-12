package run.halo.app.core.extension;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.infra.Conditions;

/**
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = "content.halo.run", version = "v1alpha1", kind = Post.KIND,
    plural = "posts", singular = "post")
@EqualsAndHashCode(callSuper = true)
public class Post extends AbstractExtension {
    public static final String KIND = "Post";

    @Schema(required = true)
    private PostSpec spec;

    @Schema(required = true)
    private PostStatus status;

    @Data
    public static class PostSpec {
        @Schema(required = true, minLength = 1)
        private String title;

        private String releaseSnapshot;

        @Schema(required = true, minLength = 1)
        private String headSnapshot;

        @Schema(required = true, minLength = 1)
        private String baseSnapshot;

        @Schema(required = true, minLength = 1)
        private String owner;

        private String template;

        private String cover;

        @Schema(required = true, defaultValue = "false")
        private Boolean deleted;

        @Schema(required = true, defaultValue = "false")
        private Boolean published;

        @Schema(required = true, defaultValue = "false")
        private Boolean pinned;

        @Schema(required = true, defaultValue = "true")
        private Boolean allowComment;

        @Schema(required = true, defaultValue = "PUBLIC")
        private VisibleEnum visible;

        @Schema(required = true, defaultValue = "1")
        private Integer version;

        @Schema(required = true, defaultValue = "0")
        private Integer priority;

        @Schema(required = true)
        private Excerpt excerpt;

        private List<String> categories;

        private List<String> tags;

        private List<Map<String, String>> htmlMetas;
    }

    @Data
    public static class PostStatus {
        @Schema(required = true)
        private String phase;

        @Schema(required = true)
        private List<Conditions> conditions;

        private String permalink;

        private String excerpt;

        @Schema(required = true, defaultValue = "false")
        private Boolean inProgress;

        private List<String> contributors;
    }

    @Data
    public static class Excerpt {

        @Schema(required = true, defaultValue = "true")
        private Boolean autoGenerate;

        private String raw;
    }

    public enum VisibleEnum {
        PUBLIC,
        INTERNAL,
        PRIVATE
    }
}
