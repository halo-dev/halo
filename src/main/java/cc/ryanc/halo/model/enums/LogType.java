package cc.ryanc.halo.model.enums;

/**
 * Log type.
 *
 * @author johnniang
 */
public enum LogType implements ValueEnum<Integer> {
    ;

    private final Integer value;

    LogType(Integer value) {
        this.value = value;
    }


    @Override
    public Integer getValue() {
        return value;
    }
}
