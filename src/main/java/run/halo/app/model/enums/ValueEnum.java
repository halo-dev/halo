package run.halo.app.model.enums;

import java.util.stream.Stream;
import org.springframework.util.Assert;

/**
 * Interface for value enum.
 *
 * @param <T> value type
 * @author johnniang
 */
public interface ValueEnum<T> {

    /**
     * Gets enum value.
     *
     * @return enum value
     */
    T getValue();

    /**
     * Converts value to corresponding enum.
     *
     * @param enumType enum type
     * @param value database value
     * @param <V> value generic
     * @param <E> enum generic
     * @return corresponding enum
     */
    static <V, E extends Enum<E> & ValueEnum<V>> E valueToEnum(Class<E> enumType, V value) {
        Assert.notNull(enumType, "enum type must not be null");
        Assert.notNull(value, "value must not be null");
        Assert.isTrue(enumType.isEnum(), "type must be an enum type");

        return Stream.of(enumType.getEnumConstants())
            .filter(item -> item.getValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("unknown database value: " + value));
    }
}
