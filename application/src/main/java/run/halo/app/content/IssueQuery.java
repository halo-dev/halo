package run.halo.app.content;

import java.util.function.Predicate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.server.ServerRequest;
import run.halo.app.core.extension.content.Issue;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.router.SortableRequest;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;

/**
 * Query parameters for issues.
 *
 * @author halo-copilot
 * @since 2.21.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IssueQuery extends SortableRequest {

    private final String username;

    public IssueQuery(ServerRequest request) {
        this(request, null);
    }

    public IssueQuery(ServerRequest request, @Nullable String username) {
        super(request.exchange());
        this.username = username;
    }

    @Nullable
    public String getStatus() {
        return queryParams.getFirst("status");
    }

    @Nullable
    public String getPriority() {
        return queryParams.getFirst("priority");
    }

    @Nullable
    public String getType() {
        return queryParams.getFirst("type");
    }

    @Nullable
    public String getKeyword() {
        return queryParams.getFirst("keyword");
    }

    /**
     * Build predicate from query.
     */
    public Predicate<Issue> toPredicate() {
        Predicate<Issue> predicate = issue -> true;

        if (username != null) {
            predicate = predicate.and(issue -> username.equals(issue.getSpec().getOwner()));
        }

        if (getStatus() != null) {
            predicate = predicate.and(issue -> getStatus().equals(issue.getSpec().getStatus()));
        }

        if (getPriority() != null) {
            predicate = predicate.and(issue -> getPriority().equals(issue.getSpec().getPriority()));
        }

        if (getType() != null) {
            predicate = predicate.and(issue -> getType().equals(issue.getSpec().getType()));
        }

        if (getKeyword() != null && !getKeyword().trim().isEmpty()) {
            predicate = predicate.and(issue -> 
                issue.getSpec().getTitle().toLowerCase().contains(getKeyword().toLowerCase()) ||
                issue.getSpec().getDescription().toLowerCase().contains(getKeyword().toLowerCase())
            );
        }

        return predicate;
    }

    /**
     * Convert to ListOptions for querying.
     */
    public ListOptions toListOptions() {
        var listOptions = new ListOptions();
        
        var labelSelectorBuilder = LabelSelector.builder();
        
        // Always exclude deleted issues unless specified
        labelSelectorBuilder.eq(Issue.DELETED_LABEL, "false");
        
        if (username != null) {
            labelSelectorBuilder.eq(Issue.OWNER_LABEL, username);
        }
        
        if (getStatus() != null) {
            labelSelectorBuilder.eq(Issue.STATUS_LABEL, getStatus());
        }
        
        listOptions.setLabelSelector(labelSelectorBuilder.build());
        
        return listOptions;
    }
    
    /**
     * Convert to PageRequest for pagination.
     */
    public PageRequestImpl toPageRequest() {
        return PageRequestImpl.of(getPage(), getSize(), getSort());
    }
}