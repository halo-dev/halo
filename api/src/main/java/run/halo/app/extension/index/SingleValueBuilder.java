package run.halo.app.extension.index;

import java.util.function.Function;
import org.springframework.util.Assert;
import run.halo.app.extension.Extension;

public class SingleValueBuilder<E extends Extension, K extends Comparable<K>>
    extends AbstractValueIndexSpecBuilder<E, K, SingleValueBuilder<E, K>> {

    private Function<E, K> indexFunc;

    SingleValueBuilder(String name, Class<K> keyType) {
        super(name, keyType);
    }

    public SingleValueBuilder<E, K> indexFunc(Function<E, K> indexFunc) {
        this.indexFunc = indexFunc;
        return this;
    }

    @Override
    public SingleValueIndexSpec<E, K> build() {
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
