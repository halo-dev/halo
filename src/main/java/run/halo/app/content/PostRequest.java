package run.halo.app.content;

import io.swagger.v3.oas.annotations.media.Schema;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Snapshot;

/**
 * @author guqing
 * @since 2.0.0
 */
public record PostRequest(@Schema(required = true) Post post,
                          @Schema(required = true) Content content) {

    public ContentRequest contentRequest() {
        Snapshot.SubjectRef subjectRef =
            Snapshot.SubjectRef.of(Post.KIND, post.getMetadata().getName());
        return new ContentRequest(subjectRef, post.getSpec().getHeadSnapshot(), content.raw,
            content.content, content.rawType);
    }

    public record Content(String raw, String content, String rawType) {
    }
}
