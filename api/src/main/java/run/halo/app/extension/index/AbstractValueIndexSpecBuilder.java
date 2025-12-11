package run.halo.app.extension.index;

import org.springframework.util.Assert;
import run.halo.app.extension.Extension;

/**
 * Abstract base implementation of {@link IndexSpecBuilder} for value indexes.
 *
 * @param <E> the type of extension
 * @param <K> the type of key
 * @param <B> the type of builder
 * @author johnniang
 * @since 2.22.0
 */
abstract class AbstractValueIndexSpecBuilder<
    E extends Extension,
    K extends Comparable<K>,
    B extends IndexSpecBuilder<E, K, B>
    > implements IndexSpecBuilder<E, K, B> {

    protected final String name;

    protected final Class<K> keyType;

    protected boolean unique = false;

    protected boolean nullable = true;

    protected AbstractValueIndexSpecBuilder(String name, Class<K> keyType) {
        Assert.hasText(name, "Index name must not be blank");
        Assert.notNull(keyType, "Key type must not be null");
        this.name = name;
        this.keyType = keyType;
    }

    public B unique(boolean unique) {
        this.unique = unique;
        return (B) this;
    }

    public B nullable(boolean nullable) {
        this.nullable = nullable;
        return (B) this;
    }

}
