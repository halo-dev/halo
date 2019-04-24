package run.halo.app.model.properties;

/**
 * Primary properties.
 *
 * @author johnniang
 * @date 4/2/19
 */
public enum PrimaryProperties implements PropertyEnum {

    IS_INSTALLED("is_installed", Boolean.class, ""),

    THEME("theme", String.class, ""),

    BIRTHDAY("birthday", Long.class, ""),
    ;

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    PrimaryProperties(String value, Class<?> type, String defaultValue) {
        this.value = value;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }

    @Override
    public String getValue() {
        return value;
    }
}
