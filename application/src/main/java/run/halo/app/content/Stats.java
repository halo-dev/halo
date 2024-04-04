package run.halo.app.content;

import lombok.Builder;
import lombok.Data;

/**
 * Stats value object.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
public class Stats {

    private Integer visit;

    private Integer upvote;

    private Integer totalComment;

    private Integer approvedComment;

    public Stats() {
    }

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
