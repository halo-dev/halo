package run.halo.app.extension.index;

import java.util.Set;
import org.springframework.lang.Nullable;
import run.halo.app.extension.Extension;

public interface MultiValueIndexSpec<E extends Extension, K extends Comparable<K>>
    extends ValueIndexSpec<E, K> {

    @Nullable
    Set<K> getValues(E extension);

    static <E extends Extension, K extends Comparable<K>> MultiValueBuilder<E, K> builder(
        String name, Class<K> keyType) {
        return new MultiValueBuilder<>(name, keyType);
    }

}
