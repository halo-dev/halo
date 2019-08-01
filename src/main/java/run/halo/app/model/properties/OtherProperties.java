package run.halo.app.model.properties;

/**
 * Other properties.
 *
 * @author johnniang
 * @author ryanwang
 * @date 4/1/19
 */
public enum OtherProperties implements PropertyEnum {

    CUSTOM_HEAD("blog_custom_head",String.class,""),

    STATISTICS_CODE("blog_statistics_code", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    OtherProperties(String value, Class<?> type, String defaultValue) {
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
