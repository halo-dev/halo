package run.halo.app.content;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.util.comparator.Comparators;
import run.halo.app.core.extension.content.Post;

/**
 * A sorter for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
public enum PostSorter {
    PUBLISH_TIME,
    CREATE_TIME;

    static final Function<Post, String> name = post -> post.getMetadata().getName();

    /**
     * Converts {@link Comparator} from {@link PostSorter} and ascending.
     *
     * @param sorter a {@link PostSorter}
     * @param ascending ascending if true, otherwise descending
     * @return a {@link Comparator} of {@link Post}
     */
    public static Comparator<Post> from(PostSorter sorter, Boolean ascending) {
        if (Objects.equals(true, ascending)) {
            return from(sorter);
        }
        return from(sorter).reversed();
    }

    /**
     * Converts {@link Comparator} from {@link PostSorter}.
     *
     * @param sorter a {@link PostSorter}
     * @return a {@link Comparator} of {@link Post}
     */
    public static Comparator<Post> from(PostSorter sorter) {
        if (sorter == null) {
            return defaultComparator();
        }
        if (CREATE_TIME.equals(sorter)) {
            Function<Post, Instant> comparatorFunc =
                post -> post.getMetadata().getCreationTimestamp();
            return Comparator.comparing(comparatorFunc)
                .thenComparing(name);
        }

        if (PUBLISH_TIME.equals(sorter)) {
            Function<Post, Instant> comparatorFunc =
                post -> post.getSpec().getPublishTime();
            return Comparator.comparing(comparatorFunc, Comparators.nullsLow())
                .thenComparing(name);
        }

        throw new IllegalArgumentException("Unsupported sort value: " + sorter);
    }

    static PostSorter convertFrom(String sort) {
        for (PostSorter sorter : values()) {
            if (sorter.name().equalsIgnoreCase(sort)) {
                return sorter;
            }
        }
        return null;
    }

    static Comparator<Post> defaultComparator() {
        Function<Post, Instant> createTime =
            post -> post.getMetadata().getCreationTimestamp();
        return Comparator.comparing(createTime)
            .thenComparing(name);
    }
}
