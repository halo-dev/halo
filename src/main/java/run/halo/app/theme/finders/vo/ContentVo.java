package run.halo.app.theme.finders.vo;

import lombok.Builder;
import lombok.Value;
import run.halo.app.core.extension.Snapshot;

/**
 * A value object for Content from {@link Snapshot}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
public class ContentVo {

    String raw;

    String content;
}
