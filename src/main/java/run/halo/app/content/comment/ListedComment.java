package run.halo.app.content.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import run.halo.app.core.extension.Comment;
import run.halo.app.extension.Extension;

/**
 * Listed comment.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
public class ListedComment {

    @Schema(required = true)
    Comment comment;

    @Schema(required = true)
    OwnerInfo owner;

    Extension subject;
}
