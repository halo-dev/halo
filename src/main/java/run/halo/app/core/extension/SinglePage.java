package run.halo.app.core.extension;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * <p>Single page extension.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = "content.halo.run", version = "v1alpha1", kind = SinglePage.KIND,
    plural = "singlepages", singular = "singlepage")
@EqualsAndHashCode(callSuper = true)
public class SinglePage extends AbstractExtension {
    public static final String KIND = "SinglePage";
    public static final String DELETED_LABEL = "content.halo.run/deleted";
    public static final String OWNER_LABEL = "content.halo.run/owner";
    public static final String VISIBLE_LABEL = "content.halo.run/visible";
    public static final String PHASE_LABEL = "content.halo.run/phase";

    @Schema(required = true)
    private SinglePageSpec spec;

    @Schema
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
        return Objects.equals(true, spec.getPublished());
    }

    @Data
    public static class SinglePageSpec {
        @Schema(required = true, minLength = 1)
        private String title;

        @Schema(required = true, minLength = 1)
        private String slug;

        /**
         * 引用到的已发布的内容，用于主题端显示.
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
        private Post.VisibleEnum visible;

        @Schema(required = true, defaultValue = "1")
        private Integer version;

        @Schema(required = true, defaultValue = "0")
        private Integer priority;

        @Schema(required = true)
        private Post.Excerpt excerpt;

        private List<Map<String, String>> htmlMetas;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class SinglePageStatus extends Post.PostStatus {

    }
}
