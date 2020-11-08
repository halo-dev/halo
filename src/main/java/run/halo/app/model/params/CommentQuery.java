package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.enums.CommentStatus;

/**
 * Comment query params.
 *
 * @author ryanwang
 * @date 2019/04/18
 */
@Data
public class CommentQuery {

    /**
     * Keyword.
     */
    private String keyword;

    /**
     * Comment status.
     */
    private CommentStatus status;
}
