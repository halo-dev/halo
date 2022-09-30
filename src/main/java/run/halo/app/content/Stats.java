package run.halo.app.content;

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
public class Stats {

    Integer visit;

    Integer upvote;

    Integer totalComment;

    Integer approvedComment;
}
