package run.halo.app.model.enums;


import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Data type enum.
 *
 * @author johnniang
 * @date 4/9/19
 */
@Slf4j
public enum DataType implements ValueEnum<Integer> {

    STRING(0),

    LONG(1),

    DOUBLE(2),

    BOOL(3);

    private final Integer value;

    DataType(Integer value) {
        this.value = value;
    }

    /**
     * Data type of string.
     *
     * @param type data type string
     * @return corresponding data type, default is `STRING` if the type is missing
     */
    public static DataType typeOf(@Nullable Object type) {
        if (type != null) {
            for (DataType datatype : values()) {
                if (datatype.name().equalsIgnoreCase(type.toString())) {
                    return datatype;
                }
            }
        }

        return STRING;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    /**
     * Converts data to corresponding type.
     *
     * @param data data to be converted must not be null
     * @return data with corresponding type
     */
    @NonNull
    public Object convertTo(@NonNull Object data) {
        Assert.notNull(data, "Data must not be null");

        try {
            switch (this) {
                case STRING:
                    return data.toString();
                case BOOL:
                    return Boolean.valueOf(data.toString());
                case LONG:
                    return Long.valueOf(data.toString());
                case DOUBLE:
                    return Double.valueOf(data.toString());
                default:
                    return data;
            }
        } catch (Exception e) {
            log.warn("Failed to convert " + data + " to corresponding type: " + this.name(), e);
            return data;
        }
    }

}
