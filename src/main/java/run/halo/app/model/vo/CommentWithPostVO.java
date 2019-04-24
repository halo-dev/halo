package run.halo.app.model.vo;

import run.halo.app.model.dto.CommentDTO;
import run.halo.app.model.dto.post.PostMinimalDTO;
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
public class CommentWithPostVO extends CommentDTO {

    private PostMinimalDTO post;
}
