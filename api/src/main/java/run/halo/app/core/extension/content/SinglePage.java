package run.halo.app.core.extension.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.MetadataUtil;

/**
 * <p>Single page extension.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = Constant.GROUP, version = Constant.VERSION, kind = SinglePage.KIND,
    plural = "singlepages", singular = "singlepage")
@EqualsAndHashCode(callSuper = true)
public class SinglePage extends AbstractExtension {

    public static final String KIND = "SinglePage";

    public static final GroupVersionKind GVK = GroupVersionKind.fromExtension(SinglePage.class);
    public static final String DELETED_LABEL = "content.halo.run/deleted";
    public static final String PUBLISHED_LABEL = "content.halo.run/published";
    public static final String LAST_RELEASED_SNAPSHOT_ANNO =
        "content.halo.run/last-released-snapshot";
    public static final String OWNER_LABEL = "content.halo.run/owner";
    public static final String VISIBLE_LABEL = "content.halo.run/visible";

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
        Map<String, String> labels = getMetadata().getLabels();
        return labels != null && labels.getOrDefault(PUBLISHED_LABEL, "false").equals("true");
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
        private Boolean publish;

        private Instant publishTime;

        @Schema(required = true, defaultValue = "false")
        private Boolean pinned;

        @Schema(required = true, defaultValue = "true")
        private Boolean allowComment;

        @Schema(required = true, defaultValue = "PUBLIC")
        private Post.VisibleEnum visible;

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

    public static void changePublishedState(SinglePage page, boolean value) {
        Map<String, String> labels = MetadataUtil.nullSafeLabels(page);
        labels.put(PUBLISHED_LABEL, String.valueOf(value));
    }
}
