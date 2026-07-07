package run.halo.app.core.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Ordered Attachment permalink match results. */
@Schema(name = "AttachmentPermalinkMatchList")
public record AttachmentPermalinkMatchList(List<AttachmentPermalinkMatchResult> items) {}
