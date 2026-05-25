package run.halo.app.content.comment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.Extension;

/**
 * Comment list item returned by the console comment API.
 *
 * @author guqing
 * @since 2.0.0
 */
@Schema(description = "Comment list item with resolved owner, subject, and counters.")
@Data
@Builder
public class ListedComment {

    @Schema(description = "Comment extension data.", requiredMode = REQUIRED)
    private Comment comment;

    @Schema(description = "Resolved owner information for display.", requiredMode = REQUIRED)
    private OwnerInfo owner;

    /** Resolved subject extension that the comment belongs to, when available. */
    private Extension subject;

    @Schema(description = "Aggregated counters for the comment.", requiredMode = REQUIRED)
    private CommentStats stats;
}
