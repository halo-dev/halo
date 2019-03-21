package cc.ryanc.halo.model.vo;

import cc.ryanc.halo.model.dto.CategoryOutputDTO;
import lombok.Data;

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
