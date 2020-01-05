package run.halo.app.model.properties;

import static run.halo.app.model.support.HaloConst.DEFAULT_THEME_ID;

/**
 * Primary properties.
 *
 * @author johnniang
 * @date 4/2/19
 */
public enum PrimaryProperties implements PropertyEnum {

    /**
     * is blog installed.
     */
    IS_INSTALLED("is_installed", Boolean.class, "false"),

    /**
     * current actived theme.
     */
    THEME("theme", String.class, DEFAULT_THEME_ID),

    /**
     * blog birthday
     */
    BIRTHDAY("birthday", Long.class, ""),

    /**
     * developer mode.
     */
    DEV_MODE("developer_mode", Boolean.class, "false");

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
