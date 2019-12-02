package run.halo.app.model.properties;

/**
 * SEO properties.
 *
 * @author johnniang
 * @author ryanwang
 * @date 4/1/19
 */
public enum SeoProperties implements PropertyEnum {

    /**
     * Site keywords meta.
     */
    KEYWORDS("seo_keywords", String.class, ""),

    /**
     * Site description meta.
     */
    DESCRIPTION("seo_description", String.class, ""),

    /**
     * Disable web spider.
     */
    SPIDER_DISABLED("seo_spider_disabled", Boolean.class, "false");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    SeoProperties(String value, Class<?> type, String defaultValue) {
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
