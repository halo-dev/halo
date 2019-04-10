package run.halo.app.model.enums;


import org.springframework.lang.Nullable;

/**
 * Data type enum.
 *
 * @author johnniang
 * @date 4/9/19
 */
public enum DataType implements ValueEnum<Integer> {

    STRING(0),

    LONG(1),

    DOUBLE(2),

    BOOL(3);

    private Integer value;

    DataType(Integer value) {
        this.value = value;
    }


    @Override
    public Integer getValue() {
        return value;
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
}
