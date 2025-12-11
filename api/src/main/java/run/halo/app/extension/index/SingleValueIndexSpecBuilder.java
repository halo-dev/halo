package run.halo.app.extension.index;

import java.util.function.Function;
import run.halo.app.extension.Extension;

/**
 * Builder for {@link SingleValueIndexSpec}.
 *
 * @param <E> the type of extension
 * @param <K> the type of index key
 * @author johnniang
 * @since 2.22.0
 */
public interface SingleValueIndexSpecBuilder<E extends Extension, K extends Comparable<K>>
    extends IndexSpecBuilder<E, K, SingleValueIndexSpecBuilder<E, K>> {

    /**
     * Sets the index function.
     *
     * @param indexFunc the index function
     * @return the builder itself
     */
    SingleValueIndexSpecBuilder<E, K> indexFunc(Function<E, K> indexFunc);

}
