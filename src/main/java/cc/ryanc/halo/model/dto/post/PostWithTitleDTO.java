package cc.ryanc.halo.model.dto.post;

import cc.ryanc.halo.model.dto.base.OutputConverter;
import cc.ryanc.halo.model.entity.Post;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Page with title only dto.
 *
 * @author johnniang
 */
@Data
@ToString
@EqualsAndHashCode
public class PostWithTitleDTO implements OutputConverter<PostWithTitleDTO, Post> {

    private Integer id;

    /**
     * 文章标题
     */
    private String title;

}
