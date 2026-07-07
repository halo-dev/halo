package run.halo.app.core.attachment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Request payload for matching URL strings against Attachment permalinks. */
@Schema(name = "AttachmentPermalinkMatchRequest")
public record AttachmentPermalinkMatchRequest(
        @Schema(requiredMode = REQUIRED) List<String> urls) {}
