package run.halo.app.content.comment;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.util.comparator.Comparators;
import run.halo.app.core.extension.content.Comment;

/**
 * Comment sorter.
 *
 * @author guqing
 * @since 2.0.0
 */
public enum CommentSorter {
    LAST_REPLY_TIME,
    REPLY_COUNT,
    CREATE_TIME;

    static final Function<Comment, String> name = comment -> comment.getMetadata().getName();

    static Comparator<Comment> from(CommentSorter sorter, Boolean ascending) {
        if (Objects.equals(true, ascending)) {
            return from(sorter);
        }
        return from(sorter).reversed();
    }

    static Comparator<Comment> from(CommentSorter sorter) {
        if (sorter == null) {
            return lastReplyTimeComparator();
        }
        if (CREATE_TIME.equals(sorter)) {
            Function<Comment, Instant> comparatorFunc =
                comment -> comment.getSpec().getCreationTime();
            return Comparator.comparing(comparatorFunc, Comparators.nullsLow())
                .thenComparing(name);
        }

        if (REPLY_COUNT.equals(sorter)) {
            Function<Comment, Integer> comparatorFunc =
                comment -> comment.getStatusOrDefault().getReplyCount();
            return Comparator.comparing(comparatorFunc, Comparators.nullsLow())
                .thenComparing(name);
        }

        if (LAST_REPLY_TIME.equals(sorter)) {
            Function<Comment, Instant> comparatorFunc =
                comment -> comment.getStatusOrDefault().getLastReplyTime();
            return Comparator.comparing(comparatorFunc, Comparators.nullsLow())
                .thenComparing(name);
        }

        throw new IllegalStateException("Unsupported sort value: " + sorter);
    }

    static CommentSorter convertFrom(String sort) {
        for (CommentSorter sorter : values()) {
            if (sorter.name().equalsIgnoreCase(sort)) {
                return sorter;
            }
        }
        return null;
    }

    static Comparator<Comment> lastReplyTimeComparator() {
        Function<Comment, Instant> comparatorFunc =
            comment -> {
                Instant lastReplyTime = comment.getStatusOrDefault().getLastReplyTime();
                return Optional.ofNullable(lastReplyTime)
                    .orElse(comment.getSpec().getCreationTime());
            };
        return Comparator.comparing(comparatorFunc, Comparators.nullsLow())
            .thenComparing(name);
    }
}
