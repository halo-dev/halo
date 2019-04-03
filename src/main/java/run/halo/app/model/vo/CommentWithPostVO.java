package run.halo.app.model.vo;

import run.halo.app.model.dto.CommentOutputDTO;
import run.halo.app.model.dto.post.PostMinimalOutputDTO;
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
