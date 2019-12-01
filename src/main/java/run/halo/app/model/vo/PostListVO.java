package run.halo.app.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.model.dto.CategoryDTO;
import run.halo.app.model.dto.PostMetaDTO;
import run.halo.app.model.dto.TagDTO;
import run.halo.app.model.dto.post.BasePostSimpleDTO;
import run.halo.app.model.entity.PostMeta;

import java.util.List;

/**
 * Post list vo.
 *
 * @author johnniang
 * @date 3/19/19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostListVO extends BasePostSimpleDTO {

    private Long commentCount;

    private List<TagDTO> tags;

    private List<CategoryDTO> categories;

    private List<PostMetaDTO> postMetas;
}
