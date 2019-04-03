package run.halo.app.model.vo;

import run.halo.app.model.dto.CommentOutputDTO;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Comment vo.
 *
 * @author johnniang
 * @date 3/25/19
 */
@Data
@ToString(callSuper = true)
public class CommentVO extends CommentOutputDTO {

    private List<CommentVO> children;

}
