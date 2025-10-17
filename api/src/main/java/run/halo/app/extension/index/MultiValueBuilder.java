package run.halo.app.extension.index;

import java.util.Set;
import java.util.function.Function;
import org.springframework.util.Assert;
import run.halo.app.extension.Extension;

public class MultiValueBuilder<E extends Extension, K extends Comparable<K>>
    extends AbstractValueIndexSpecBuilder<E, K, MultiValueBuilder<E, K>> {

    private Function<E, Set<K>> indexFunc;

    MultiValueBuilder(String name, Class<K> keyType) {
        super(name, keyType);
    }

    public MultiValueBuilder<E, K> indexFunc(Function<E, Set<K>> indexFunc) {
        this.indexFunc = indexFunc;
        return this;
    }

    @Override
    public MultiValueIndexSpec<E, K> build() {
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
