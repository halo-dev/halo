package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.enums.PostStatus;

/**
 * Post query.
 *
 * @author johnniang
 * @date 4/10/19
 */
@Data
public class PostQuery {

    /**
     * Keyword.
     */
    private String keyword;

    /**
     * Post status.
     */
    private PostStatus status;

    /**
     * Category id.
     */
    private Integer categoryId;

}
