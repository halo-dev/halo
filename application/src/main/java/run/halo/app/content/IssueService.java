package run.halo.app.content;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Issue;
import run.halo.app.extension.ListResult;

/**
 * Service for {@link Issue}.
 *
 * @author halo-copilot
 * @since 2.21.0
 */
public interface IssueService {

    /**
     * List issues by query.
     */
    Mono<ListResult<ListedIssue>> listIssues(IssueQuery query);

    /**
     * Create issue.
     */
    Mono<Issue> createIssue(Issue issue);

    /**
     * Update issue.
     */
    Mono<Issue> updateIssue(Issue issue);

    /**
     * Get issue by name and owner.
     */
    Mono<Issue> getByUsername(String issueName, String username);

    /**
     * Delete issue by name and owner.
     */
    Mono<Issue> deleteByUsername(String issueName, String username);
}