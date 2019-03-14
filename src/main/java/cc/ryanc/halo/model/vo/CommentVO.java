package cc.ryanc.halo.model.vo;

import cc.ryanc.halo.model.dto.CommentOutputDTO;
import cc.ryanc.halo.model.dto.post.PostMinimalOutputDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Comment vo.
 *
 * @author johnniang
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class CommentVO extends CommentOutputDTO {

    private PostMinimalOutputDTO post;
}
