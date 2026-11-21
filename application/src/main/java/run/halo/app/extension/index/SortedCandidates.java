package run.halo.app.extension.index;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Orders candidates by an index key without sorting an entry per candidate with a multi-column comparator.
 *
 * <p>Candidates are grouped by their index key and each group is ordered by the tie breaker. Since the key is equal
 * inside a group, concatenating the groups in key order yields the same order as sorting every candidate by the key and
 * then by the tie breaker.
 */
final class SortedCandidates {

    private SortedCandidates() {}

    /**
     * Orders the candidates by their index key, placing candidates without a key first when ascending and last when
     * descending.
     *
     * @param candidates the candidate primary keys
     * @param index the index buckets
     * @param keyAccessor the key of a candidate, or {@code null} when it has none
     * @param scannedEntries how many entries walking the buckets would touch, or {@link Long#MAX_VALUE} when that
     *     cannot be known cheaply; walking is only used when it is cheaper than grouping the candidates
     * @param descending whether the key is ordered descending
     * @param tieBreaker the comparator for candidates that share the same key
     * @return the ordered primary keys, lazily
     */
    static <K extends Comparable<K>> Stream<String> order(
            Set<String> candidates,
            ConcurrentNavigableMap<K, Set<String>> index,
            Function<String, K> keyAccessor,
            long scannedEntries,
            boolean descending,
            Comparator<String> tieBreaker) {
        var keyOfCandidate = new HashMap<String, K>(Math.max(16, candidates.size() * 2));
        var candidatesWithoutKey = new ArrayList<String>();
        for (var candidate : candidates) {
            var key = keyAccessor.apply(candidate);
            if (key == null) {
                candidatesWithoutKey.add(candidate);
            } else {
                keyOfCandidate.put(candidate, key);
            }
        }
        candidatesWithoutKey.sort(tieBreaker);
        var ordered = scannedEntries <= 4L * candidates.size()
                ? walkBuckets(index, keyOfCandidate, descending, tieBreaker)
                : groupByKey(keyOfCandidate, descending, tieBreaker);
        // A null key is the smallest key: first ascending, last descending.
        return descending
                ? Stream.concat(ordered, candidatesWithoutKey.stream())
                : Stream.concat(candidatesWithoutKey.stream(), ordered);
    }

    private static <K extends Comparable<K>> Stream<String> walkBuckets(
            ConcurrentNavigableMap<K, Set<String>> index,
            HashMap<String, K> keyOfCandidate,
            boolean descending,
            Comparator<String> tieBreaker) {
        var entries = descending ? index.descendingMap().entrySet() : index.entrySet();
        return entries.stream().flatMap(entry -> {
            var bucket = new ArrayList<String>();
            for (var candidate : entry.getValue()) {
                // A multi-value candidate belongs to several buckets but is only emitted at its sort key.
                if (Objects.equals(keyOfCandidate.get(candidate), entry.getKey())) {
                    bucket.add(candidate);
                }
            }
            if (bucket.isEmpty()) {
                return Stream.<String>empty();
            }
            bucket.sort(tieBreaker);
            return bucket.stream();
        });
    }

    private static <K extends Comparable<K>> Stream<String> groupByKey(
            HashMap<String, K> keyOfCandidate, boolean descending, Comparator<String> tieBreaker) {
        var groups = new HashMap<K, List<String>>();
        keyOfCandidate.forEach((candidate, key) ->
                groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(candidate));
        var keys = new ArrayList<>(groups.keySet());
        keys.sort(descending ? Comparator.<K>reverseOrder() : Comparator.<K>naturalOrder());
        return keys.stream().flatMap(key -> {
            var group = groups.get(key);
            group.sort(tieBreaker);
            return group.stream();
        });
    }
}
