package run.halo.app.model.properties;

/**
 * Other properties.
 *
 * @author johnniang
 * @date 4/1/19
 */
public enum OtherProperties implements PropertyEnum {

    API_ENABLED("api_enabled", Boolean.class),

    API_TOKEN("api_token", String.class),

    STATISTICS_CODE("statistics_code", String.class),
    ;

    private final String value;

    private final Class<?> type;

    OtherProperties(String value, Class<?> type) {
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
    }
}
