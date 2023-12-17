package run.halo.app.theme.finders.vo;

import lombok.Builder;
import lombok.Value;

/**
 * comment stats value object.
 *
 * @author LIlGG
 * @since 2.0.0
 */
@Value
@Builder
public class CommentStatsVo {
    Integer upvote;

    public static CommentStatsVo empty() {
        return CommentStatsVo.builder()
            .upvote(0)
            .build();
    }
}
