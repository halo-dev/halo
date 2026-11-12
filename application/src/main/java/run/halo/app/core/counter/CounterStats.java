package run.halo.app.core.counter;

import run.halo.app.core.extension.Counter;

/**
 * Aggregated statistics of all counters.
 *
 * @param visits total visit count
 * @param comments total comment count
 * @param approvedComments total approved comment count
 * @param upvotes total upvote count
 */
public record CounterStats(long visits, long comments, long approvedComments, long upvotes) {

    static final CounterStats EMPTY = new CounterStats(0, 0, 0, 0);

    CounterStats plus(Counter counter) {
        return new CounterStats(
                visits + value(counter.getVisit()),
                comments + value(counter.getTotalComment()),
                approvedComments + value(counter.getApprovedComment()),
                upvotes + value(counter.getUpvote()));
    }

    private static long value(Integer value) {
        return value == null ? 0 : value;
    }
}
