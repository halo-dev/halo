package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.dto.base.OutputConverter;
import cc.ryanc.halo.model.entity.Category;
import lombok.Data;

/**
 * Category output dto.
 *
 * @author johnniang
 * @date 3/19/19
 */
@Data
public class CategoryOutputDTO implements OutputConverter<CategoryOutputDTO, Category> {

    private String name;

    private String snakeName;

    private String description;

    private Integer parentId;
}
