package run.halo.app.extension.index;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.springframework.util.Assert;
import run.halo.app.extension.Extension;

@Deprecated(forRemoval = true, since = "2.22.0")
class DefaultIndexAttribute<E extends Extension, K extends Comparable<K>>
    implements IndexAttribute<E, K> {

    private final Class<E> objectType;

    private final Class<K> keyType;

    private final Function<E, Set<K>> valuesFunc;

    public DefaultIndexAttribute(Function<E, Set<K>> valuesFunc, Class<E> objectType,
        Class<K> keyType) {
        Assert.notNull(valuesFunc, "Values function must not be null");
        Assert.notNull(objectType, "Cannot resolve object type");
        Assert.notNull(keyType, "Cannot resolve key type");
        this.valuesFunc = valuesFunc;
        this.objectType = objectType;
        this.keyType = keyType;
    }

    @Override
    public Class<E> getObjectType() {
        return objectType;
    }

    @Override
    public Class<K> getKeyType() {
        return keyType;
    }

    @Override
    public Set<K> getValues(E e) {
        if (!checkType(e)) {
            throw new IllegalArgumentException("Object type does not match");
        }
        return Optional.ofNullable(this.valuesFunc.apply(e)).orElse(Set.of());
    }

    private boolean checkType(Extension object) {
        return getObjectType().isInstance(object);
    }
}
