package run.halo.app.theme.finders.vo;

import java.util.Objects;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import run.halo.app.core.extension.Category;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.MetadataOperator;

/**
 * A value object for {@link Category}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
@EqualsAndHashCode
public class CategoryVo {

    MetadataOperator metadata;

    Category.CategorySpec spec;

    Category.CategoryStatus status;

    /**
     * Convert {@link Category} to {@link CategoryVo}.
     *
     * @param category category extension
     * @return category value object
     */
    public static CategoryVo from(Category category) {
        return CategoryVo.builder()
            .metadata(category.getMetadata())
            .spec(category.getSpec())
            .status(category.getStatus())
            .build();
    }

    /**
     * Gets the number of posts under the current category and its sub categories.
     *
     * @return the number of posts
     */
    public long postCount() {
        if (this.status == null || this.status.getPosts() == null) {
            return 0;
        }
        return this.status.getPosts().stream()
            .filter(post -> Objects.equals(true, post.getPublished())
                && Post.VisibleEnum.PUBLIC.equals(post.getVisible()))
            .count();
    }
}
