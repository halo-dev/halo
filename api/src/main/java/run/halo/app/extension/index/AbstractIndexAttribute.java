package run.halo.app.extension.index;

import lombok.EqualsAndHashCode;
import org.springframework.util.Assert;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GVK;

@EqualsAndHashCode
public abstract class AbstractIndexAttribute<E extends Extension> implements IndexAttribute {
    private final Class<E> objectType;

    /**
     * Creates a new {@link AbstractIndexAttribute} for the given object type.
     *
     * @param objectType must not be {@literal null}.
     */
    public AbstractIndexAttribute(Class<E> objectType) {
        Assert.notNull(objectType, "Object type must not be null");
        Assert.state(isValidExtension(objectType),
            "Invalid extension type, make sure you have annotated it with @" + GVK.class
                .getSimpleName());
        this.objectType = objectType;
    }

    @Override
    public Class<E> getObjectType() {
        return this.objectType;
    }

    boolean isValidExtension(Class<? extends Extension> type) {
        return type.getAnnotation(GVK.class) != null;
    }
}
