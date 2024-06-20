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
    kind = KIND, plural = "categories", singular = "category")
@EqualsAndHashCode(callSuper = true)
public class Category extends AbstractExtension {

    public static final String KIND = "Category";

    public static final GroupVersionKind GVK = GroupVersionKind.fromExtension(Category.class);

    @Schema(requiredMode = REQUIRED)
    private CategorySpec spec;

    @Schema
    private CategoryStatus status;

    @JsonIgnore
    public boolean isDeleted() {
        return getMetadata().getDeletionTimestamp() != null;
    }

    @Data
    public static class CategorySpec {

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String displayName;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String slug;

        private String description;

        private String cover;

        @Schema(requiredMode = NOT_REQUIRED, maxLength = 255)
        private String template;

        /**
         * <p>Used to specify the template for the posts associated with the category.</p>
         * <p>The priority is not as high as that of the post.</p>
         * <p>If the post also specifies a template, the post's template will prevail.</p>
         */
        @Schema(requiredMode = NOT_REQUIRED, maxLength = 255)
        private String postTemplate;

        @Schema(requiredMode = REQUIRED, defaultValue = "0")
        private Integer priority;

        private List<String> children;
    }

    @JsonIgnore
    public CategoryStatus getStatusOrDefault() {
        if (this.status == null) {
            this.status = new CategoryStatus();
        }
        return this.status;
    }

    @Data
    public static class CategoryStatus {

        private String permalink;

        /**
         * 包括当前和其下所有层级的文章数量 (depth=max).
         */
        public Integer postCount;

        /**
         * 包括当前和其下所有层级的已发布且公开的文章数量 (depth=max).
         */
        public Integer visiblePostCount;
    }
}
