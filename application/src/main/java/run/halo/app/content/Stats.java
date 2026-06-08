package run.halo.app.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Aggregated counters for content resources.
 *
 * @author guqing
 * @since 2.0.0
 */
@Schema(description = "Aggregated visit, upvote, and comment counters for a content resource.")
@Data
public class Stats {

    /** Total visit count. */
    private Integer visit;

    /** Total upvote count. */
    private Integer upvote;

    /** Total comment count, including comments that may not be approved. */
    private Integer totalComment;

    /** Total approved comment count. */
    private Integer approvedComment;

    public Stats() {}

    @Builder
    public Stats(Integer visit, Integer upvote, Integer totalComment, Integer approvedComment) {
        this.visit = visit;
        this.upvote = upvote;
        this.totalComment = totalComment;
        this.approvedComment = approvedComment;
    }

    public static Stats empty() {
        return Stats.builder()
                .visit(0)
                .upvote(0)
                .totalComment(0)
                .approvedComment(0)
                .build();
    }
}
