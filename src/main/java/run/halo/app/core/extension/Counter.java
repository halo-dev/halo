package run.halo.app.core.extension;

import io.micrometer.core.instrument.Meter;
import java.util.Collection;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.metrics.MeterUtils;

/**
 * A counter for number of requests by extension resource name.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@GVK(group = "metrics.halo.run", version = "v1alpha1", kind = "Counter", plural = "counters",
    singular = "counter")
@EqualsAndHashCode(callSuper = true)
public class Counter extends AbstractExtension {

    private Integer visit;

    private Integer upvote;

    private Integer downvote;

    private Integer totalComment;

    private Integer approvedComment;

    /**
     * Populate counter data from {@link Meter}s.
     *
     * @param meters counter meters
     */
    public <T extends Meter> void populateFrom(Collection<T> meters) {
        populateDefaultValue();
        for (Meter meter : meters) {
            if (meter instanceof io.micrometer.core.instrument.Counter meterCounter) {
                if (MeterUtils.isVisitCounter(meterCounter)) {
                    this.visit = (int) meterCounter.count();
                } else if (MeterUtils.isUpvoteCounter(meterCounter)) {
                    this.upvote = (int) meterCounter.count();
                } else if (MeterUtils.isDownvoteCounter(meterCounter)) {
                    this.downvote = (int) meterCounter.count();
                } else if (MeterUtils.isTotalCommentCounter(meterCounter)) {
                    this.totalComment = (int) meterCounter.count();
                } else if (MeterUtils.isApprovedCommentCounter(meterCounter)) {
                    this.approvedComment = (int) meterCounter.count();
                }
            }
        }
    }

    private void populateDefaultValue() {
        this.visit = 0;
        this.upvote = 0;
        this.downvote = 0;
        this.totalComment = 0;
        this.approvedComment = 0;
    }
}
