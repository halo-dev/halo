package run.halo.app.core.attachment;

import io.swagger.v3.oas.annotations.media.Schema;

/** Result for one Attachment permalink match input. */
@Schema(name = "AttachmentPermalinkMatchResult")
public record AttachmentPermalinkMatchResult(String url, boolean matched) {}
