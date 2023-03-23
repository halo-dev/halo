package run.halo.app.content;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.util.comparator.Comparators;
import run.halo.app.core.extension.content.SinglePage;

/**
 * A sorter for {@link SinglePage}.
 *
 * @author guqing
 * @since 2.0.0
 */
public enum SinglePageSorter {
    PUBLISH_TIME,
    CREATE_TIME;

    static final Function<SinglePage, String> name = page -> page.getMetadata().getName();

    /**
     * Converts {@link Comparator} from {@link SinglePageSorter} and ascending.
     *
     * @param sorter a {@link SinglePageSorter}
     * @param ascending ascending if true, otherwise descending
     * @return a {@link Comparator} of {@link SinglePage}
     */
    public static Comparator<SinglePage> from(SinglePageSorter sorter, Boolean ascending) {
        if (Objects.equals(true, ascending)) {
            return from(sorter);
        }
        return from(sorter).reversed();
    }

    /**
     * Converts {@link Comparator} from {@link SinglePageSorter}.
     *
     * @param sorter a {@link SinglePageSorter}
     * @return a {@link Comparator} of {@link SinglePage}
     */
    public static Comparator<SinglePage> from(SinglePageSorter sorter) {
        if (sorter == null) {
            return defaultComparator();
        }
        if (CREATE_TIME.equals(sorter)) {
            Function<SinglePage, Instant> comparatorFunc =
                page -> page.getMetadata().getCreationTimestamp();
            return Comparator.comparing(comparatorFunc)
                .thenComparing(name);
        }

        if (PUBLISH_TIME.equals(sorter)) {
            Function<SinglePage, Instant> comparatorFunc =
                page -> page.getSpec().getPublishTime();
            return Comparator.comparing(comparatorFunc, Comparators.nullsLow())
                .thenComparing(name);
        }

        throw new IllegalArgumentException("Unsupported sort value: " + sorter);
    }

    static SinglePageSorter convertFrom(String sort) {
        for (SinglePageSorter sorter : values()) {
            if (sorter.name().equalsIgnoreCase(sort)) {
                return sorter;
            }
        }
        return null;
    }

    static Comparator<SinglePage> defaultComparator() {
        Function<SinglePage, Instant> createTime =
            page -> page.getMetadata().getCreationTimestamp();
        return Comparator.comparing(createTime)
            .thenComparing(name);
    }
}
