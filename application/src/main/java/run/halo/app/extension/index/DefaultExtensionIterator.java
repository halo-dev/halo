package run.halo.app.extension.index;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.Extension;

/**
 * Default implementation of {@link ExtensionIterator}.
 *
 * @param <E> the type of the extension.
 * @author guqing
 * @since 2.12.0
 */
public class DefaultExtensionIterator<E extends Extension> implements ExtensionIterator<E> {
    static final int DEFAULT_PAGE_SIZE = 500;
    private final ExtensionPaginatedLister<E> lister;
    private Pageable currentPageable;
    private List<E> currentData;
    private int currentIndex;

    public DefaultExtensionIterator(ExtensionPaginatedLister<E> lister) {
        this(PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by("name")), lister);
    }

    /**
     * Constructs a new DefaultExtensionIterator with the given lister.
     *
     * @param lister the lister to use to load data.
     */
    public DefaultExtensionIterator(Pageable initPageable, ExtensionPaginatedLister<E> lister) {
        this.lister = lister;
        this.currentPageable = initPageable;
        this.currentData = loadData();
    }

    private List<E> loadData() {
        Page<E> page = lister.list(currentPageable);
        currentPageable = page.hasNext() ? page.nextPageable() : null;
        return page.getContent();
    }

    @Override
    public boolean hasNext() {
        if (currentIndex < currentData.size()) {
            return true;
        }
        if (currentPageable == null) {
            return false;
        }
        currentData = loadData();
        currentIndex = 0;
        return !currentData.isEmpty();
    }

    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return currentData.get(currentIndex++);
    }
}
