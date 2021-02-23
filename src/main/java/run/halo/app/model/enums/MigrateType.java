package run.halo.app.model.enums;

/**
 * Migrate type.
 *
 * @author ryanwang
 * @date 2019-03-12
 */
public enum MigrateType implements ValueEnum<Integer> {

    /**
     * Halo
     */
    HALO(0);

    private final Integer value;

    MigrateType(Integer value) {
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
