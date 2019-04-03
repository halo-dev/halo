package run.halo.app.model.vo;

import run.halo.app.model.dto.CategoryOutputDTO;
import lombok.Data;
import run.halo.app.model.dto.CategoryOutputDTO;

import java.util.List;

/**
 * Category vo.
 *
 * @author johnniang
 * @date 3/21/19
 */
@Data
public class CategoryVO extends CategoryOutputDTO {

    private List<CategoryVO> children;
}
