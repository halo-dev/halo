package cc.ryanc.halo.model.params;

import cc.ryanc.halo.model.dto.base.InputConverter;
import cc.ryanc.halo.model.entity.Category;
import cc.ryanc.halo.utils.SlugUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Category param.
 *
 * @author johnniang
 * @date 3/21/19
 */
@Data
public class CategoryParam implements InputConverter<Category> {

    /**
     * 分类名称
     */
    @NotBlank(message = "Category name must not be blank")
    @Size(max = 50, message = "Length of category name must not be more than {max}")
    private String name;

    /**
     * 缩略名
     */
    @Size(max = 50, message = "Length of category slug name must not be more than {max}")
    private String slugName;

    /**
     * 描述
     */
    @Size(max = 100, message = "Length of category description must not be more than {max}")
    private String description;

    /**
     * 上级目录
     */
    private Integer parentId;

    @Override
    public Category convertTo() {
        // Handle default value
        if (StringUtils.isBlank(slugName)) {
            slugName = SlugUtils.slugify(name);
        }

        if (parentId == null || parentId < 0) {
            parentId = 0;
        }

        return InputConverter.super.convertTo();
    }
}
