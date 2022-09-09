package run.halo.app.theme.finders.vo;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.util.Assert;
import run.halo.app.core.extension.Category;

/**
 * A tree vo for {@link Category}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CategoryTreeVo extends BaseCategoryVo {

    private List<CategoryTreeVo> children;

    private String parentName;

    /**
     * Convert {@link CategoryVo} to {@link CategoryTreeVo}.
     *
     * @param category category value object
     * @return category tree value object
     */
    public static CategoryTreeVo from(CategoryVo category) {
        Assert.notNull(category, "The category must not be null");
        return CategoryTreeVo.builder()
            .name(category.getName())
            .displayName(category.getDisplayName())
            .slug(category.getSlug())
            .description(category.getDescription())
            .cover(category.getCover())
            .template(category.getTemplate())
            .priority(category.getPriority())
            .permalink(category.getPermalink())
            .build();
    }
}
