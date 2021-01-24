package run.halo.app.model.vo;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.CategoryDTO;

/**
 * Category vo.
 *
 * @author johnniang
 * @date 3/21/19
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CategoryVO extends CategoryDTO {

    private List<CategoryVO> children;
}
