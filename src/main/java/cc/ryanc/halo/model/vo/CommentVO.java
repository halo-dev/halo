package cc.ryanc.halo.model.vo;

import cc.ryanc.halo.model.dto.CommentOutputDTO;
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
