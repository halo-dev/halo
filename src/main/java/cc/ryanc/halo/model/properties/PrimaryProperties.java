package cc.ryanc.halo.model.properties;

/**
 * Primary properties.
 *
 * @author johnniang
 * @date 4/2/19
 */
public enum PrimaryProperties implements PropertyEnum {

    IS_INSTALLED("is_installed", Boolean.class),

    THEME("theme", String.class),

    BIRTHDAY("birthday", Long.class),
    ;

    private final String value;

    private final Class<?> type;

    PrimaryProperties(String value, Class<?> type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }}
