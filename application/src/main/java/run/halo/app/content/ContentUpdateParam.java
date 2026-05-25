package run.halo.app.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request body used to update the editable content of a post or single page.
 *
 * @param version Expected current head snapshot version; conflicts create a new head snapshot.
 * @param raw source text stored by the editor
 * @param content rendered HTML or normalized content derived from {@code raw}
 * @param rawType editor/source format, for example HTML or Markdown
 */
@Schema(description = "Content update payload for post and single page editors.")
public record ContentUpdateParam(
        Long version,

        @Schema(description = "Source text stored by the editor.", requiredMode = REQUIRED)
        String raw,

        @Schema(description = "Rendered HTML or normalized content derived from raw.", requiredMode = REQUIRED)
        String content,

        @Schema(
                description = "Editor/source format of the raw content, for example HTML or Markdown.",
                requiredMode = REQUIRED)
        String rawType) {

    public static ContentUpdateParam from(Content content) {
        return new ContentUpdateParam(null, content.raw(), content.content(), content.rawType());
    }
}
