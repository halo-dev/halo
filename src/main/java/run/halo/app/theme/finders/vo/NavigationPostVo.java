package run.halo.app.theme.finders.vo;

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

    PostVo previous;

    PostVo current;

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
