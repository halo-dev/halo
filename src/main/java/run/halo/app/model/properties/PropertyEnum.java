package run.halo.app.model.properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import run.halo.app.model.enums.ValueEnum;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
     * @param value string value must not be blank
     * @param type  property value type must not be null
     * @param <T>   property value type
     * @return property value
     */
    @SuppressWarnings("unchecked")
    static <T> T convertTo(@NonNull String value, @NonNull Class<T> type) {
        Assert.notNull(value, "Value must not be null");
        Assert.notNull(type, "Type must not be null");

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
            return (T) Double.valueOf(value);
        }

        if (type.isAssignableFrom(Float.class)) {
            return (T) Float.valueOf(value);
        }

        // Should never happen
        throw new UnsupportedOperationException("Unsupported convention for blog property type:" + type.getName() + " provided");
    }

    /**
     * Converts to value with corresponding type
     *
     * @param value        value
     * @param propertyEnum property enum must not be null
     * @return property value
     */
    @SuppressWarnings("unchecked")
    static Object convertTo(@Nullable String value, @NonNull PropertyEnum propertyEnum) {
        Assert.notNull(propertyEnum, "Property enum must not be null");

        if (StringUtils.isBlank(value)) {
            // Set default value
            value = propertyEnum.defaultValue();
        }

        try {
            if (propertyEnum.getType().isAssignableFrom(Enum.class)) {
                Class<Enum> type = (Class<Enum>) propertyEnum.getType();
                Enum result = convertToEnum(value, type);
                return result != null ? result : value;
            }

            return convertTo(value, propertyEnum.getType());
        } catch (Exception e) {
            // Return value
            return value;
        }
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

    static Map<String, PropertyEnum> getValuePropertyEnumMap() {
        // Get all properties
        List<Class<? extends PropertyEnum>> propertyEnumClasses = new LinkedList<>();
        propertyEnumClasses.add(AliYunProperties.class);
        propertyEnumClasses.add(AttachmentProperties.class);
        propertyEnumClasses.add(BlogProperties.class);
        propertyEnumClasses.add(CommentProperties.class);
        propertyEnumClasses.add(EmailProperties.class);
        propertyEnumClasses.add(OtherProperties.class);
        propertyEnumClasses.add(PostProperties.class);
        propertyEnumClasses.add(PrimaryProperties.class);
        propertyEnumClasses.add(QnYunProperties.class);
        propertyEnumClasses.add(SeoProperties.class);
        propertyEnumClasses.add(UpYunProperties.class);
        propertyEnumClasses.add(ApiProperties.class);

        Map<String, PropertyEnum> result = new HashMap<>();

        propertyEnumClasses.forEach(propertyEnumClass -> {
            PropertyEnum[] propertyEnums = propertyEnumClass.getEnumConstants();

            for (PropertyEnum propertyEnum : propertyEnums) {
                result.put(propertyEnum.getValue(), propertyEnum);
            }
        });

        return result;
    }

    /**
     * Get property type.
     *
     * @return property type
     */
    Class<?> getType();

    /**
     * Default value.
     *
     * @return default value
     */
    String defaultValue();
}
