package cc.ryanc.halo.model.vo;

import cc.ryanc.halo.model.dto.CommentOutputDTO;
import cc.ryanc.halo.model.dto.post.PostMinimalOutputDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Comment list with post vo.
 *
 * @author johnniang
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class CommentWithPostVO extends CommentOutputDTO {

    private PostMinimalOutputDTO post;
}
