package run.halo.app.content.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.Extension;

/**
 * Listed comment.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@Builder
public class ListedComment {

    @Schema(required = true)
    private Comment comment;

    @Schema(required = true)
    private OwnerInfo owner;

    private Extension subject;

    @Schema(required = true)
    private CommentStats stats;
}
