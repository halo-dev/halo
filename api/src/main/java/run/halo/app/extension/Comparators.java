package run.halo.app.extension;

import java.time.Instant;
import java.util.Comparator;

public enum Comparators {
    ;

    public static <E extends Extension> Comparator<E> compareCreationTimestamp(boolean asc) {
        var comparator =
            Comparator.<E, Instant>comparing(e -> e.getMetadata().getCreationTimestamp());
        return asc ? comparator : comparator.reversed();
    }

    public static <E extends Extension> Comparator<E> compareName(boolean asc) {
        var comparator = Comparator.<E, String>comparing(e -> e.getMetadata().getName());
        return asc ? comparator : comparator.reversed();
    }

    /**
     * Get a nulls comparator.
     *
     * @param isAscending is ascending
     * @return if ascending, return nulls high, else return nulls low
     */
    public static Comparator<Object> nullsComparator(boolean isAscending) {
        return isAscending
            ? org.springframework.util.comparator.Comparators.nullsHigh()
            : org.springframework.util.comparator.Comparators.nullsLow();
    }
}
