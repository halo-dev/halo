package run.halo.app.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.dto.post.BasePostMinimalDTO;

/**
 * PostComment list with post vo.
 *
 * @author johnniang
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class SheetCommentWithSheetVO extends BaseCommentDTO {

    private BasePostMinimalDTO sheet;
}
