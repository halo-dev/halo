package run.halo.app.theme.finders.vo;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

/**
 * Post navigation vo to hold previous and next item.
 *
 * @param previous Previous post. It's publishing time is earlier than current post.
 * @param next Next post. It's publishing time is later than current post.
 * @author guqing
 * @author johnniang
 * @since 2.0.0
 */
@Builder
public record NavigationPostVo(

    @Schema(requiredMode = NOT_REQUIRED)
    @Nullable
    ListedPostVo previous,

    @Schema(requiredMode = NOT_REQUIRED)
    @Nullable
    ListedPostVo next

) {
    /**
     * Indicates whether it has next post.
     *
     * @return true if it has next post, false otherwise
     */
    public boolean hasNext() {
        return next != null;
    }

    /**
     * Indicates whether it has previous post.
     *
     * @return true if it has previous post, false otherwise
     */
    public boolean hasPrevious() {
        return previous != null;
    }

    public static NavigationPostVo empty() {
        return NavigationPostVo.builder().build();
    }
}
