package run.halo.app.model.params;

import lombok.Data;

/**
 * Post query.
 *
 * @author cuiyusong
 * @date 5/15/21
 */
@Data
public class PostQueryContent {

    /**
     * Keyword.
     */
    private String keyword;
    /**
     * Category id.
     */
    private Integer categoryId;

}
