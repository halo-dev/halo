package run.halo.app.content;

import lombok.Data;
import lombok.experimental.Accessors;
import run.halo.app.core.extension.content.Issue;

/**
 * Listed issue for list result.
 *
 * @author halo-copilot
 * @since 2.21.0
 */
@Data
@Accessors(chain = true)
public class ListedIssue {
    private Issue issue;
    private Stats stats;

    /**
     * Issue statistic.
     */
    @Data
    public static class Stats {
        private Integer commentsCount = 0;
        private Integer upvotesCount = 0;
    }
}