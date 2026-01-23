package run.halo.app.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

public record ContentUpdateParam(Long version, @Schema(requiredMode = REQUIRED) String raw,
                                 @Schema(requiredMode = REQUIRED) String content,
                                 @Schema(requiredMode = REQUIRED) String rawType) {

    public static ContentUpdateParam from(Content content) {
        return new ContentUpdateParam(null, content.raw(), content.content(),
            content.rawType());
    }
}
