package run.halo.app.content.comment;

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
public class CommentStats {

    Integer upvote;

    public static CommentStats empty() {
        return CommentStats.builder()
            .upvote(0)
            .build();
    }
}
