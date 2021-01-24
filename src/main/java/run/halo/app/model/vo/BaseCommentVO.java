package run.halo.app.model.vo;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.BaseCommentDTO;

/**
 * Base comment vo.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BaseCommentVO extends BaseCommentDTO {

    List<BaseCommentVO> children;
}
