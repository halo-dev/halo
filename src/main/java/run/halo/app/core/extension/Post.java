package run.halo.app.core.extension;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.infra.Condition;

/**
 * <p>Post extension.</p>
 *
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
    public static final String CATEGORIES_ANNO = "content.halo.run/categories";
    public static final String TAGS_ANNO = "content.halo.run/tags";
    public static final String DELETED_LABEL = "content.halo.run/deleted";
    public static final String OWNER_LABEL = "content.halo.run/owner";
    public static final String VISIBLE_LABEL = "content.halo.run/visible";
    public static final String PHASE_LABEL = "content.halo.run/phase";

    @Schema(required = true)
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

    @Data
    public static class PostSpec {
        @Schema(required = true, minLength = 1)
        private String title;

        @Schema(required = true, minLength = 1)
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

        @Schema(required = true, defaultValue = "false")
        private Boolean deleted;

        @Schema(required = true, defaultValue = "false")
        private Boolean published;

        private Instant publishTime;

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

        @Schema
        private List<Condition> conditions;

        private String permalink;

        private String excerpt;

        private Boolean inProgress;

        private List<String> contributors;

        @JsonIgnore
        public List<Condition> getConditionsOrDefault() {
            if (this.conditions == null) {
                this.conditions = new ArrayList<>();
            }
            return conditions;
        }
    }

    @Data
    public static class Excerpt {

        @Schema(required = true, defaultValue = "true")
        private Boolean autoGenerate;

        private String raw;
    }

    public enum PostPhase {
        DRAFT,
        PENDING_APPROVAL,
        PUBLISHED
    }

    public enum VisibleEnum {
        PUBLIC,
        INTERNAL,
        PRIVATE
    }
}
