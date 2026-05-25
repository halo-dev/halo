package run.halo.app.content.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Aggregated counters for a comment or reply.
 *
 * @author LIlGG
 * @since 2.0.0
 */
@Schema(description = "Aggregated counters for a comment or reply.")
@Value
@Builder
public class CommentStats {

    /** Total upvote count. */
    Integer upvote;

    public static CommentStats empty() {
        return CommentStats.builder().upvote(0).build();
    }
}
