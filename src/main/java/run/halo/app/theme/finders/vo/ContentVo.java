package run.halo.app.theme.finders.vo;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import run.halo.app.core.extension.content.Snapshot;

/**
 * A value object for Content from {@link Snapshot}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@ToString
@Builder
public class ContentVo {

    String raw;

    String content;

    /**
     * Empty content object.
     */
    public static ContentVo empty() {
        return ContentVo.builder()
            .raw("")
            .content("")
            .build();
    }
}
