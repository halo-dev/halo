package run.halo.app.extension.index;

import org.springframework.util.Assert;
import run.halo.app.extension.Extension;

public abstract class AbstractValueIndexSpecBuilder<
    E extends Extension,
    K extends Comparable<K>,
    B extends AbstractValueIndexSpecBuilder<E, K, B>
    > implements IndexSpecBuilder<E, K> {

    protected final String name;

    protected final Class<K> keyType;

    protected boolean unique = false;

    protected boolean nullable = true;

    protected AbstractValueIndexSpecBuilder(String name, Class<K> keyType) {
        Assert.notNull(name, "Index name must not be null");
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
