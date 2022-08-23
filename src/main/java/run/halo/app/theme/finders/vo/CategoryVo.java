package run.halo.app.theme.finders.vo;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import run.halo.app.core.extension.Category;

/**
 * A value object for {@link Category}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
public class CategoryVo {
    String name;

    String displayName;

    String slug;

    String description;

    String cover;

    String template;

    Integer priority;

    List<String> children;

    String permalink;

    List<String> posts;

    /**
     * Convert {@link Category} to {@link CategoryVo}.
     *
     * @param category category extension
     * @return category value object
     */
    public static CategoryVo from(Category category) {
        Category.CategorySpec spec = category.getSpec();
        return CategoryVo.builder()
            .name(category.getMetadata().getName())
            .displayName(spec.getDisplayName())
            .slug(spec.getSlug())
            .description(spec.getDescription())
            .cover(spec.getCover())
            .template(spec.getTemplate())
            .priority(spec.getPriority())
            .children(spec.getChildren())
            .permalink(category.getStatusOrDefault().getPermalink())
            .build();
    }
}
