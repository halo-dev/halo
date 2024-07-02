package run.halo.app.extension.index;

import java.util.Set;
import java.util.function.Function;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import run.halo.app.extension.Extension;

@EqualsAndHashCode(callSuper = true)
public class FunctionalIndexAttribute<E extends Extension>
    extends AbstractIndexAttribute<E> {

    @EqualsAndHashCode.Exclude
    private final Function<E, String> valueFunc;

    /**
     * Creates a new {@link FunctionalIndexAttribute} for the given object type and value function.
     *
     * @param objectType must not be {@literal null}.
     * @param valueFunc value function must not be {@literal null}.
     */
    public FunctionalIndexAttribute(Class<E> objectType,
        Function<E, String> valueFunc) {
        super(objectType);
        Assert.notNull(valueFunc, "Value function must not be null");
        this.valueFunc = valueFunc;
    }

    @Override
    public Set<String> getValues(Extension object) {
        var value = getValue(object);
        return value == null ? Set.of() : Set.of(value);
    }

    /**
     * Gets the value for the given object.
     *
     * @param object the object to get the value for.
     * @return returns the value for the given object.
     */
    @Nullable
    public String getValue(Extension object) {
        if (getObjectType().isInstance(object)) {
            return valueFunc.apply(getObjectType().cast(object));
        }
        throw new IllegalArgumentException("Object type does not match");
    }
}
