package run.halo.app.model.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import run.halo.app.model.enums.PostStatus;

/**
 * Category id and post id with status projection.
 *
 * @author guqing
 * @date 2022-04-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryIdPostStatusProjection {
    /**
     * category id.
     */
    private Integer categoryId;

    /**
     * post id.
     */
    private Integer postId;


    /**
     * post status.
     */
    private PostStatus postStatus;
}
