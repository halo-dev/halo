package run.halo.app.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Full content payload returned by content snapshot endpoints.
 *
 * @param raw source text stored by the editor
 * @param content rendered HTML or normalized content derived from {@code raw}
 * @param rawType editor/source format, for example HTML or Markdown
 */
@Schema(description = "Full content payload reconstructed from a content snapshot.")
public record Content(
        @Schema(description = "Source text stored by the editor.", requiredMode = REQUIRED)
        String raw,

        @Schema(description = "Rendered HTML or normalized content derived from raw.", requiredMode = REQUIRED)
        String content,

        @Schema(
                description = "Editor/source format of the raw content, for example HTML or Markdown.",
                requiredMode = REQUIRED)
        String rawType) {}
