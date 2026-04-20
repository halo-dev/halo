package run.halo.app.theme.finders.vo;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import run.halo.app.core.extension.content.Category;
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
public class CategoryVo implements ExtensionVoOperator {

    @Schema(requiredMode = REQUIRED)
    MetadataOperator metadata;

    Category.CategorySpec spec;

    Category.CategoryStatus status;

    Integer postCount;

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
            .postCount(category.getStatusOrDefault().getVisiblePostCount())
            .build();
    }
}
