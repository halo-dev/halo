package run.halo.app.content.comment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

/** Body-only update for an existing comment or reply. */
public record CommentContentRequest(
        @Schema(requiredMode = REQUIRED, minLength = 1) String raw,
        @Schema(requiredMode = REQUIRED, minLength = 1) String content,

        @Schema(requiredMode = REQUIRED, description = "Resource version when the editor was opened.")
        Long version) {}
