package run.halo.app.model.properties;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import run.halo.app.model.enums.ValueEnum;

/**
 * Property enum.
 *
 * @author johnniang
 * @date 3/26/19
 */
public interface PropertyEnum extends ValueEnum<String> {

    /**
     * Converts to value with corresponding type
     *
     * @param value string value must not be null
     * @param type  property value type must not be null
     * @param <T>   property value type
     * @return property value
     */
    @SuppressWarnings("unchecked")
    static <T> T convertTo(@NonNull String value, @NonNull Class<T> type) {
        Assert.hasText(value, "Property value must not be blank");

        if (!isSupportedType(type)) {
            throw new IllegalArgumentException("Unsupported blog property type: " + type);
        }

        if (type.isAssignableFrom(String.class)) {
            return (T) value;
        }

        if (type.isAssignableFrom(Integer.class)) {
            return (T) Integer.valueOf(value);
        }

        if (type.isAssignableFrom(Long.class)) {
            return (T) Long.valueOf(value);
        }

        if (type.isAssignableFrom(Boolean.class)) {
            return (T) Boolean.valueOf(value);
        }

        if (type.isAssignableFrom(Short.class)) {
            return (T) Short.valueOf(value);
        }

        if (type.isAssignableFrom(Byte.class)) {
            return (T) Byte.valueOf(value);
        }

        if (type.isAssignableFrom(Double.class)) {
            return (T) Byte.valueOf(value);
        }

        if (type.isAssignableFrom(Float.class)) {
            return (T) Float.valueOf(value);
        }

        // Should never happen
        throw new UnsupportedOperationException("Unsupported convention for blog property type:" + type.getName() + " provided");
    }

    /**
     * Converts to enum.
     *
     * @param value string value must not be null
     * @param type  propertye value enum type must not be null
     * @param <T>   property value enum type
     * @return property enum value or null
     */
    @Nullable
    static <T extends Enum<T>> T convertToEnum(@NonNull String value, @NonNull Class<T> type) {
        Assert.hasText(value, "Property value must not be blank");

        try {
            return Enum.valueOf(type, value.toUpperCase());
        } catch (Exception e) {
            // Ignore this exception
            return null;
        }
    }

    /**
     * Check the type is support by the blog property.
     *
     * @param type type to check
     * @return true if supports; false else
     */
    static boolean isSupportedType(Class<?> type) {
        return type != null && (
                type.isAssignableFrom(String.class)
                        || type.isAssignableFrom(Number.class)
                        || type.isAssignableFrom(Integer.class)
                        || type.isAssignableFrom(Long.class)
                        || type.isAssignableFrom(Boolean.class)
                        || type.isAssignableFrom(Short.class)
                        || type.isAssignableFrom(Byte.class)
                        || type.isAssignableFrom(Double.class)
                        || type.isAssignableFrom(Float.class)
                        || type.isAssignableFrom(Enum.class)
                        || type.isAssignableFrom(ValueEnum.class)
        );
    }

    /**
     * Get property type.
     *
     * @return property type
     */
    Class<?> getType();
}
