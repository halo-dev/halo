package run.halo.app.content.comment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

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

    @Schema(requiredMode = REQUIRED)
    private Comment comment;

    @Schema(requiredMode = REQUIRED)
    private OwnerInfo owner;

    private Extension subject;

    @Schema(requiredMode = REQUIRED)
    private CommentStats stats;
}
