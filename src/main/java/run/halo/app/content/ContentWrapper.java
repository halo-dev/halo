package run.halo.app.content;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author guqing
 * @since 2.0.0
 */
public record ContentWrapper(@Schema(required = true) String snapshotName,
                             @Schema(required = true) String raw,
                             @Schema(required = true) String content,
                             @Schema(required = true) String rawType) {
}
