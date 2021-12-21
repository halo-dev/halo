package run.halo.app.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.CategoryDTO;

/**
 * Category vo.
 *
 * @author johnniang
 * @author guqing
 * @date 3/21/19
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CategoryVO extends CategoryDTO {

    @JsonIgnore
    private Set<Integer> postIds;

    private List<CategoryVO> children;
}
