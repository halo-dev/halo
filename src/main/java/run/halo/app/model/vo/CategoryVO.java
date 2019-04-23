package run.halo.app.model.vo;

import run.halo.app.model.dto.CategoryDTO;
import lombok.Data;

import java.util.List;

/**
 * Category vo.
 *
 * @author johnniang
 * @date 3/21/19
 */
@Data
public class CategoryVO extends CategoryDTO {

    private List<CategoryVO> children;
}
