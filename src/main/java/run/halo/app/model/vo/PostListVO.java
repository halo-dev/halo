package run.halo.app.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.model.dto.BaseMetaDTO;
import run.halo.app.model.dto.CategoryDTO;
import run.halo.app.model.dto.TagDTO;
import run.halo.app.model.dto.post.BasePostSimpleDTO;

import java.util.List;

/**
 * Post list vo.
 *
 * @author johnniang
 * @author guqing
 * @date 2019-03-19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostListVO extends BasePostSimpleDTO {

    private Long commentCount;

    private List<TagDTO> tags;

    private List<CategoryDTO> categories;

    private List<BaseMetaDTO> postMetas;
}
