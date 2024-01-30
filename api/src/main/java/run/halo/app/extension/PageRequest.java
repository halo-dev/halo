package run.halo.app.extension;

import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

/**
 * <p>{@link PageRequest} is an interface for pagination information.</p>
 * <p>Page number starts from 1.</p>
 * <p>if page size is 0, it means no pagination and all results will be returned.</p>
 *
 * @author guqing
 * @see PageRequestImpl
 * @since 2.12.0
 */
public interface PageRequest {
    int getPageNumber();

    int getPageSize();

    PageRequest previous();

    PageRequest next();

    /**
     * Returns the previous {@link PageRequest} or the first {@link PageRequest} if the current one
     * already is the first one.
     *
     * @return a new {@link org.springframework.data.domain.PageRequest} with
     * {@link #getPageNumber()} - 1 as {@link #getPageNumber()}
     */
    PageRequest previousOrFirst();

    /**
     * Returns the {@link PageRequest} requesting the first page.
     *
     * @return a new {@link org.springframework.data.domain.PageRequest} with
     * {@link #getPageNumber()} = 1 as {@link #getPageNumber()}
     */
    PageRequest first();

    /**
     * Creates a new {@link PageRequest} with {@code pageNumber} applied.
     *
     * @param pageNumber 1-based page index.
     * @return a new {@link org.springframework.data.domain.PageRequest}
     */
    PageRequest withPage(int pageNumber);

    PageRequestImpl withSort(Sort sort);

    boolean hasPrevious();

    Sort getSort();

    /**
     * Returns the current {@link Sort} or the given one if the current one is unsorted.
     *
     * @param sort must not be {@literal null}.
     * @return the current {@link Sort} or the given one if the current one is unsorted.
     */
    default Sort getSortOr(Sort sort) {
        Assert.notNull(sort, "Fallback Sort must not be null");
        return getSort().isSorted() ? getSort() : sort;
    }
}
