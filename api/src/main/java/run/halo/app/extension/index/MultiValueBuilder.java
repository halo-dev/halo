package run.halo.app.extension.index;

import java.util.Set;
import java.util.function.Function;
import org.springframework.util.Assert;
import run.halo.app.extension.Extension;

/**
 * Builder for {@link MultiValueIndexSpec}.
 *
 * @param <E> the type of extension
 * @param <K> the type of index key
 * @author johnniang
 * @since 2.22.0
 */
class MultiValueBuilder<E extends Extension, K extends Comparable<K>>
    extends AbstractValueIndexSpecBuilder<E, K, MultiValueIndexSpecBuilder<E, K>>
    implements MultiValueIndexSpecBuilder<E, K> {

    private Function<E, Set<K>> indexFunc;

    MultiValueBuilder(String name, Class<K> keyType) {
        super(name, keyType);
    }

    @Override
    public MultiValueIndexSpecBuilder<E, K> indexFunc(Function<E, Set<K>> indexFunc) {
        this.indexFunc = indexFunc;
        return this;
    }

    @Override
    public ValueIndexSpec<E, K> build() {
        Assert.hasText(name, "Index name must not be blank");
        Assert.notNull(keyType, "Key type must not be null");
        Assert.notNull(indexFunc, "Index function must not be null");

        return new MultiValueIndexSpec<>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean isUnique() {
                return unique;
            }

            @Override
            public boolean isNullable() {
                return nullable;
            }

            @Override
            public Class<K> getKeyType() {
                return keyType;
            }

            @Override
            public Set<K> getValues(E extension) {
                return indexFunc.apply(extension);
            }
        };
    }
}
