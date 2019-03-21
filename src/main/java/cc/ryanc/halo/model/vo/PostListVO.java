package cc.ryanc.halo.model.vo;

import cc.ryanc.halo.model.dto.CategoryOutputDTO;
import cc.ryanc.halo.model.dto.TagOutputDTO;
import cc.ryanc.halo.model.dto.post.PostSimpleOutputDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Post list vo.
 *
 * @author johnniang
 * @date 3/19/19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostListVO extends PostSimpleOutputDTO {

    private List<TagOutputDTO> tags;

    private List<CategoryOutputDTO> categories;
}
