package run.halo.app.theme.finders.vo;

import lombok.Builder;
import lombok.Value;

/**
 * Post cursor vo to hold previous and next item.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
public class PostCursorVo {

    PostVo previous;

    PostVo current;

    PostVo next;

    public boolean hasNext() {
        return next != null;
    }

    public boolean hasPrevious() {
        return previous != null;
    }

    public static PostCursorVo empty() {
        return PostCursorVo.builder().build();
    }
}
