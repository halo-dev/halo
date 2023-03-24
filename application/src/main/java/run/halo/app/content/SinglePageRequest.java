package run.halo.app.content;

import io.swagger.v3.oas.annotations.media.Schema;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.Ref;

/**
 * A request parameter for {@link SinglePage}.
 *
 * @author guqing
 * @since 2.0.0
 */
public record SinglePageRequest(@Schema(required = true) SinglePage page,
                                @Schema(required = true) Content content) {

    public ContentRequest contentRequest() {
        Ref subjectRef = Ref.of(page);
        return new ContentRequest(subjectRef, page.getSpec().getHeadSnapshot(), content.raw,
            content.content, content.rawType);
    }

    public record Content(String raw, String content, String rawType) {
    }
}
