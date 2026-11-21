package run.halo.app.extension.index;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

/**
 * An index that can order primary keys by its key.
 *
 * <p>Ordering through the index avoids building and sorting an entry per candidate. Only the first sort order is
 * resolved here; candidates sharing the same key are ordered by the tie breaker, which carries the remaining sort
 * orders.
 */
interface SortableIndex {

    /**
     * Orders the given candidates by this index's key.
     *
     * <p>The returned stream is lazy, so callers that only need the first page do not order every candidate.
     *
     * @param candidates the primary keys to order
     * @param descending whether the key is ordered descending
     * @param tieBreaker the comparator for candidates that share the same key
     * @return the ordered primary keys
     */
    Stream<String> sortedStream(Set<String> candidates, boolean descending, Comparator<String> tieBreaker);
}
