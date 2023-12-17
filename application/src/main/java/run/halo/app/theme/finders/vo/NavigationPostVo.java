package run.halo.app.theme.finders.vo;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Post navigation vo to hold previous and next item.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
public class NavigationPostVo {

    @Schema(requiredMode = NOT_REQUIRED)
    PostVo previous;

    PostVo current;

    @Schema(requiredMode = NOT_REQUIRED)
    PostVo next;

    public boolean hasNext() {
        return next != null;
    }

    public boolean hasPrevious() {
        return previous != null;
    }

    public static NavigationPostVo empty() {
        return NavigationPostVo.builder().build();
    }
}
