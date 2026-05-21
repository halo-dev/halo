package run.halo.app.content.comment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import run.halo.app.core.extension.content.Reply;

/**
 * Reply list item returned by the console reply API.
 *
 * @author guqing
 * @since 2.0.0
 */
@Schema(description = "Reply list item with resolved owner and counters.")
@Data
@Builder
public class ListedReply {

    @Schema(description = "Reply extension data.", requiredMode = REQUIRED)
    private Reply reply;

    @Schema(description = "Resolved owner information for display.", requiredMode = REQUIRED)
    private OwnerInfo owner;

    @Schema(description = "Aggregated counters for the reply.", requiredMode = REQUIRED)
    private CommentStats stats;
}
