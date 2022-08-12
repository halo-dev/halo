package run.halo.app.content;

import io.swagger.v3.oas.annotations.media.Schema;
import run.halo.app.core.extension.Post;

/**
 * @author guqing
 * @since 2.0.0
 */
public record PostRequest(@Schema(required = true) Post post,
                          @Schema(required = true) ContentRequest content) {

}
