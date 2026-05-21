package run.halo.app.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Ref;

/**
 * Post metadata/spec and content data for creating or updating a post from the console editor.
 *
 * @author guqing
 * @since 2.0.0
 */
@Schema(description = "Post editor payload containing the post extension and its editable content.")
public record PostRequest(
        @Schema(description = "Post extension to create or update.", requiredMode = REQUIRED)
        Post post,

        @Schema(description = "Editable content associated with the post.", requiredMode = REQUIRED)
        ContentUpdateParam content) {

    public ContentRequest contentRequest() {
        Ref subjectRef = Ref.of(post);
        return new ContentRequest(
                subjectRef,
                post.getSpec().getHeadSnapshot(),
                content.version(),
                content.raw(),
                content.content(),
                content.rawType());
    }
}
