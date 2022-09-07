package run.halo.app.content;

import io.swagger.v3.oas.annotations.media.Schema;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.core.extension.Snapshot;

/**
 * A request parameter for {@link SinglePage}.
 *
 * @author guqing
 * @since 2.0.0
 */
public record SinglePageRequest(@Schema(required = true) SinglePage page,
                                @Schema(required = true) Content content) {

    public ContentRequest contentRequest() {
        Snapshot.SubjectRef subjectRef =
            Snapshot.SubjectRef.of(SinglePage.KIND, page.getMetadata().getName());
        return new ContentRequest(subjectRef, page.getSpec().getHeadSnapshot(), content.raw,
            content.content, content.rawType);
    }

    public record Content(String raw, String content, String rawType) {
    }
}
