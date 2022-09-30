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
}
