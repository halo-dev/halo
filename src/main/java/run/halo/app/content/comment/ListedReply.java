package run.halo.app.content.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import run.halo.app.core.extension.Reply;

/**
 * Listed reply for {@link Reply}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
public class ListedReply {

    @Schema(required = true)
    Reply reply;

    @Schema(required = true)
    OwnerInfo owner;
}
