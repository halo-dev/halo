package run.halo.app.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.post.BasePostSimpleDTO;

/**
 * Sheet list dto.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SheetListVO extends BasePostSimpleDTO {

    private Long commentCount;
}
