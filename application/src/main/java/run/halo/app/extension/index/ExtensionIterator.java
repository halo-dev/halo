package run.halo.app.extension.index;

import java.util.Iterator;
import run.halo.app.extension.Extension;

/**
 * An iterator over a collection of extensions, it is used to iterate extensions in a paginated
 * way to avoid loading all extensions into memory at once.
 *
 * @param <E> the type of the extension.
 * @author guqing
 * @see DefaultExtensionIterator
 * @since 2.12.0
 */
public interface ExtensionIterator<E extends Extension> extends Iterator<E> {

    /**
     * Get the total size of the extensions that this iterator iterates over.
     *
     * @return the total size of the extensions.
     */
    long size();
}
