package run.halo.app.content.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import run.halo.app.core.extension.content.Reply;

/**
 * Listed reply for {@link Reply}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@Builder
public class ListedReply {

    @Schema(required = true)
    private Reply reply;

    @Schema(required = true)
    private OwnerInfo owner;

    @Schema(required = true)
    private CommentStats stats;
}
