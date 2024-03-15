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
@FunctionalInterface
public interface ExtensionPaginatedLister<E extends Extension> {

    /**
     * List extensions with pagination.
     *
     * @param pageable pageable
     * @return page of extensions
     */
    Page<E> list(Pageable pageable);
}
