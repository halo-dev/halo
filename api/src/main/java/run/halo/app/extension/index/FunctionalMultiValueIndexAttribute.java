package run.halo.app.extension.index;

import java.util.Set;
import java.util.function.Function;
import lombok.EqualsAndHashCode;
import org.springframework.util.Assert;
import run.halo.app.extension.Extension;

@EqualsAndHashCode(callSuper = true)
public class FunctionalMultiValueIndexAttribute<E extends Extension>
    extends AbstractIndexAttribute<E> {

    @EqualsAndHashCode.Exclude
    private final Function<E, Set<String>> valueFunc;

    /**
     * Creates a new {@link FunctionalIndexAttribute} for the given object type and value function.
     *
     * @param objectType object type must not be {@literal null}.
     * @param valueFunc value function must not be {@literal null}.
     */
    public FunctionalMultiValueIndexAttribute(Class<E> objectType,
        Function<E, Set<String>> valueFunc) {
        super(objectType);
        Assert.notNull(valueFunc, "Value function must not be null");
        this.valueFunc = valueFunc;
    }

    @Override
    public Set<String> getValues(Extension object) {
        if (getObjectType().isInstance(object)) {
            return getNonNullValues(getObjectType().cast(object));
        }
        throw new IllegalArgumentException("Object type does not match");
    }

    private Set<String> getNonNullValues(E object) {
        var values = valueFunc.apply(object);
        return values == null ? Set.of() : values;
    }
}
