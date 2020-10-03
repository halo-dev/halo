package run.halo.app.model.properties;

/**
 * Api properties.
 *
 * @author ryanwang
 * @date 2019-06-25
 */
public enum ApiProperties implements PropertyEnum {

    /**
     * api_enabled
     */
    API_ENABLED("api_enabled", Boolean.class, "false"),

    /**
     * api_access_key
     */
    API_ACCESS_KEY("api_access_key", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    ApiProperties(String value, Class<?> type, String defaultValue) {
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
