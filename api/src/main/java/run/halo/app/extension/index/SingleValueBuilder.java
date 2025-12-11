package run.halo.app.extension.index;

import java.util.function.Function;
import org.springframework.util.Assert;
import run.halo.app.extension.Extension;

/**
 * Single value index specification builder.
 *
 * @param <E> the type of extension
 * @param <K> the type of index key
 * @author johnniang
 * @since 2.22.0
 */
class SingleValueBuilder<E extends Extension, K extends Comparable<K>>
    extends AbstractValueIndexSpecBuilder<E, K, SingleValueIndexSpecBuilder<E, K>>
    implements SingleValueIndexSpecBuilder<E, K> {

    private Function<E, K> indexFunc;

    SingleValueBuilder(String name, Class<K> keyType) {
        super(name, keyType);
    }

    @Override
    public SingleValueBuilder<E, K> indexFunc(Function<E, K> indexFunc) {
        this.indexFunc = indexFunc;
        return this;
    }

    @Override
    public ValueIndexSpec<E, K> build() {
        Assert.hasText(name, "Index name must not be blank");
        Assert.notNull(keyType, "Key type must not be null");
        Assert.notNull(indexFunc, "Index function must not be null");

        return new SingleValueIndexSpec<>() {
            @Override
            public K getValue(E extension) {
                return indexFunc.apply(extension);
            }

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
        };
    }
}
