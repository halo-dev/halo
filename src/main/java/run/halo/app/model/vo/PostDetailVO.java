package run.halo.app.model.vo;

import run.halo.app.model.dto.post.PostDetailOutputDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.model.dto.post.PostDetailOutputDTO;

import java.util.Set;

/**
 * Post vo.
 *
 * @author johnniang
 * @date 3/21/19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostDetailVO extends PostDetailOutputDTO {

    private Set<Integer> tagIds;

    private Set<Integer> categoryIds;
}
