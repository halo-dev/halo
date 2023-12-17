package run.halo.app.theme.finders.vo;

import lombok.Builder;
import lombok.Data;

/**
 * A value object for site stats.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@Builder
public class SiteStatsVo {

    private Integer visit;

    private Integer upvote;

    private Integer comment;

    private Integer post;

    private Integer category;

    public static SiteStatsVo empty() {
        return SiteStatsVo.builder()
            .visit(0)
            .upvote(0)
            .comment(0)
            .post(0)
            .category(0)
            .build();
    }
}
