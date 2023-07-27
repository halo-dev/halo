package run.halo.app.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Ref;

/**
 * @author guqing
 * @since 2.0.0
 */
public record PostRequest(@Schema(requiredMode = REQUIRED) Post post,
                          @Schema(requiredMode = REQUIRED) Content content) {

    public ContentRequest contentRequest() {
        Ref subjectRef = Ref.of(post);
        return new ContentRequest(subjectRef, post.getSpec().getHeadSnapshot(), content.raw,
            content.content, content.rawType);
    }

    public record Content(String raw, String content, String rawType) {
    }
}
