package cc.ryanc.halo.model.vo;

import cc.ryanc.halo.model.dto.post.PostDetailOutputDTO;
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
public class PostDetailVO extends PostDetailOutputDTO {

    private Set<Integer> tagIds;

    private Set<Integer> categoryIds;
}
