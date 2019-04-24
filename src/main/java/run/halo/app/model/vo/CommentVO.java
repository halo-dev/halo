package run.halo.app.model.vo;

import lombok.Data;
import lombok.ToString;
import run.halo.app.model.dto.CommentDTO;

import java.util.List;

/**
 * Comment vo.
 *
 * @author johnniang
 * @date 3/25/19
 */
@Data
@ToString(callSuper = true)
@Deprecated
public class CommentVO extends CommentDTO {

    private List<CommentVO> children;

}
