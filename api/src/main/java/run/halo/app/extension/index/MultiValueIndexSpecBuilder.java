package run.halo.app.extension.index;

import java.util.Set;
import java.util.function.Function;
import run.halo.app.extension.Extension;

/**
 * Builder for {@link MultiValueIndexSpec}.
 *
 * @param <E> the type of extension
 * @param <K> the type of index key
 * @author johnniang
 * @since 2.22.0
 */
public interface MultiValueIndexSpecBuilder<E extends Extension, K extends Comparable<K>>
    extends IndexSpecBuilder<E, K, MultiValueIndexSpecBuilder<E, K>> {

    /**
     * Sets the index function.
     *
     * @param indexFunc the index function
     * @return the builder itself
     */
    MultiValueIndexSpecBuilder<E, K> indexFunc(Function<E, Set<K>> indexFunc);

}
