package run.halo.app.core.extension;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * @author guqing
 * @see <a href="https://github.com/halo-dev/halo/issues/2322">issue#2322</a>
 * @since 2.0.0
 */
@Data
@ToString(callSuper = true)
@GVK(group = "content.halo.run", version = "v1alpha1",
    kind = "Category", plural = "categories", singular = "category")
@EqualsAndHashCode(callSuper = true)
public class Category extends AbstractExtension {

    @Schema(required = true)
    private CategorySpec spec;

    @Schema(required = true)
    private CategoryStatus status;

    @Data
    public static class CategorySpec {
        @Schema(required = true)
        private String displayName;

        private String description;

        private String cover;

        private String template;

        @Schema(required = true, defaultValue = "0")
        private Integer priority;

        private List<String> children;
    }

    @Data
    public static class CategoryStatus {

        private String permalink;

        /**
         * 包括当前和其下所有层级的文章 name (depth=max)
         */
        private List<String> posts;
    }
}
