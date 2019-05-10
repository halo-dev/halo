package run.halo.app.model.dto;

import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Category;
import lombok.Data;

/**
 * Category output dto.
 *
 * @author johnniang
 * @date 3/19/19
 */
@Data
public class CategoryDTO implements OutputConverter<CategoryDTO, Category> {

    private Integer id;

    private String name;

    private String slugName;

    private String description;

    private Integer parentId;
}
