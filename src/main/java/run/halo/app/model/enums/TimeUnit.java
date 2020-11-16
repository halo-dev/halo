package run.halo.app.model.enums;

/**
 * @author Wh1te
 * @date 2020-10-19
 */
public enum TimeUnit implements ValueEnum<Integer> {

    /**
     * 天
     */
    DAY(0),

    /**
     * 小时
     */
    HOUR(1);

    private final Integer value;

    TimeUnit(Integer value) {
        this.value = value;
    }

    /**
     * Get enum value.
     *
     * @return enum value
     */
    @Override
    public Integer getValue() {
        return value;
    }
}

