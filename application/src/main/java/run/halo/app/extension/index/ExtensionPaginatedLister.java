package run.halo.app.extension.index;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import run.halo.app.extension.Extension;

/**
 * List extensions with pagination, used for {@link ExtensionIterator}.
 *
 * @author guqing
 * @since 2.12.0
 */
public interface ExtensionPaginatedLister {

    /**
     * List extensions with pagination.
     *
     * @param pageable pageable
     * @param <E> extension type
     * @return page of extensions
     */
    <E extends Extension> Page<E> list(Pageable pageable);
}
