package run.halo.app.model.enums;

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
}
