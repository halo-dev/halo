package run.halo.app.content.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.content.IssueQuery;
import run.halo.app.content.IssueService;
import run.halo.app.content.ListedIssue;
import run.halo.app.core.extension.content.Issue;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;
import run.halo.app.infra.exception.NotFoundException;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

/**
 * Issue service implementation.
 *
 * @author halo-copilot
 * @since 2.21.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private final ReactiveExtensionClient client;

    @Override
    public Mono<ListResult<ListedIssue>> listIssues(IssueQuery query) {
        return client.listBy(Issue.class, query.toListOptions(), query.toPageRequest())
            .flatMap(list -> Mono.fromCallable(() -> {
                    var listedIssues = list.get()
                        .map(this::toListedIssue)
                        .sorted(defaultComparator())
                        .toList();
                    return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(), listedIssues);
                }));
    }

    @Override
    public Mono<Issue> createIssue(Issue issue) {
        Assert.notNull(issue, "Issue must not be null");
        
        // Set default values
        if (issue.getSpec().getStatus() == null) {
            issue.getSpec().setStatus("OPEN");
        }
        if (issue.getSpec().getPriority() == null) {
            issue.getSpec().setPriority("LOW");
        }
        if (issue.getSpec().getType() == null) {
            issue.getSpec().setType("BUG");
        }
        if (issue.getSpec().getDeleted() == null) {
            issue.getSpec().setDeleted(false);
        }

        // Set metadata labels
        var labels = issue.getMetadata().getLabels();
        if (labels != null) {
            labels.put(Issue.DELETED_LABEL, "false");
            labels.put(Issue.OWNER_LABEL, issue.getSpec().getOwner());
            labels.put(Issue.STATUS_LABEL, issue.getSpec().getStatus());
        }

        return client.create(issue);
    }

    @Override
    public Mono<Issue> updateIssue(Issue issue) {
        Assert.notNull(issue, "Issue must not be null");
        return client.update(issue);
    }

    @Override
    public Mono<Issue> getByUsername(String issueName, String username) {
        Assert.hasText(issueName, "Issue name must not be blank");
        Assert.hasText(username, "Username must not be blank");

        return client.get(Issue.class, issueName)
            .filter(issue -> Objects.equals(issue.getSpec().getOwner(), username))
            .switchIfEmpty(Mono.error(
                () -> new NotFoundException("Issue not found or access denied")));
    }

    @Override
    public Mono<Issue> deleteByUsername(String issueName, String username) {
        return getByUsername(issueName, username)
            .flatMap(issue -> {
                issue.getSpec().setDeleted(true);
                if (issue.getMetadata().getLabels() != null) {
                    issue.getMetadata().getLabels().put(Issue.DELETED_LABEL, "true");
                }
                return client.update(issue);
            });
    }

    private ListedIssue toListedIssue(Issue issue) {
        var listedIssue = new ListedIssue()
            .setIssue(issue);
        
        // Set stats (placeholder for now)
        var stats = new ListedIssue.Stats();
        listedIssue.setStats(stats);

        return listedIssue;
    }

    private Comparator<ListedIssue> defaultComparator() {
        Function<ListedIssue, Instant> creationTime = issue ->
            issue.getIssue().getMetadata().getCreationTimestamp();
        return Comparator.comparing(creationTime).reversed();
    }
}