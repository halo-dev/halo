package run.halo.app.model.vo;

import run.halo.app.model.dto.post.PostDetailDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * Post vo.
 *
 * @author johnniang
 * @date 3/21/19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostDetailVO extends PostDetailDTO {

    private Set<Integer> tagIds;

    private Set<Integer> categoryIds;
}
