package run.halo.app.theme.finders.vo;

import lombok.Builder;
import lombok.Value;

/**
 * Stats value object.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
public class StatsVo {

    Integer visit;

    Integer upvote;

    Integer comment;

    public static StatsVo empty() {
        return StatsVo.builder()
            .visit(0)
            .upvote(0)
            .comment(0)
            .build();
    }
}
