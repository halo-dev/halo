package run.halo.app.core.extension;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Metadata;

/**
 * Counter extension that stores aggregate interaction counts for a resource.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@GVK(group = "metrics.halo.run", version = "v1alpha1", kind = "Counter", plural = "counters", singular = "counter")
@EqualsAndHashCode(callSuper = true)
public class Counter extends AbstractExtension {

    /** Number of visits recorded for the resource. */
    private Integer visit;

    /** Number of upvotes recorded for the resource. */
    private Integer upvote;

    /** Number of downvotes recorded for the resource. */
    private Integer downvote;

    /** Total number of comments recorded for the resource. */
    private Integer totalComment;

    /** Number of approved comments recorded for the resource. */
    private Integer approvedComment;

    public static Counter emptyCounter(String name) {
        Counter counter = new Counter();
        counter.setMetadata(new Metadata());
        counter.getMetadata().setName(name);
        counter.setUpvote(0);
        counter.setTotalComment(0);
        counter.setApprovedComment(0);
        counter.setVisit(0);
        return counter;
    }
}
