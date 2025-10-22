package run.halo.app.extension.index;

import org.springframework.lang.Nullable;
import run.halo.app.extension.Extension;

/**
 * Single value index specification.
 *
 * @param <E> the type of extension
 * @param <K> the type of key
 * @author johnniang
 * @since 2.22.0
 */
interface SingleValueIndexSpec<E extends Extension, K extends Comparable<K>>
    extends ValueIndexSpec<E, K> {

    @Nullable
    K getValue(E extension);

    static <E extends Extension, K extends Comparable<K>> SingleValueBuilder<E, K> builder(
        String name, Class<K> keyType
    ) {
        return new SingleValueBuilder<>(name, keyType);
    }

}
