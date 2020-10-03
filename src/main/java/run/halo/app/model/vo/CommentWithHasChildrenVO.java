package run.halo.app.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.BaseCommentDTO;

/**
 * Comment including replied count.
 *
 * @author johnniang
 * @date 19-5-14
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommentWithHasChildrenVO extends BaseCommentDTO {

    private boolean hasChildren;
}
