package run.halo.app.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.Ref;

/**
 * Single page metadata/spec and content data for creating or updating a single page from the console editor.
 *
 * @author guqing
 * @since 2.0.0
 */
@Schema(description = "Single page editor payload containing the page extension and its editable content.")
public record SinglePageRequest(
        @Schema(description = "Single page extension to create or update.", requiredMode = REQUIRED)
        SinglePage page,

        @Schema(description = "Editable content associated with the single page.", requiredMode = REQUIRED)
        ContentUpdateParam content) {

    public ContentRequest contentRequest() {
        Ref subjectRef = Ref.of(page);
        return new ContentRequest(
                subjectRef,
                page.getSpec().getHeadSnapshot(),
                content.version(),
                content.raw(),
                content.content(),
                content.rawType());
    }
}
