package run.halo.app.model.properties;

/**
 * Other properties.
 *
 * @author johnniang
 * @date 4/1/19
 */
public enum OtherProperties implements PropertyEnum {

    API_ENABLED("api_enabled", Boolean.class, "false"),

    API_ACCESS_KEY("api_access_key", String.class, ""),

    CUSTOM_HEAD("blog_custom_head",String.class,""),

    STATISTICS_CODE("blog_statistics_code", String.class, ""),

    /**
     * 是否禁止爬虫
     */
    SPIDER_DISABLED("spider_disabled", Boolean.class, "false");

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
