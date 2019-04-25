package run.halo.app.model.properties;

/**
 * Post properties.
 *
 * @author johnniang
 * @date 4/1/19
 */
public enum PostProperties implements PropertyEnum {

    SUMMARY_LENGTH("post_summary_length", Integer.class, "150"),

    RSS_PAGE_SIZE("rss_page_size", Integer.class, "20"),

    INDEX_PAGE_SIZE("post_index_page_size", Integer.class, "10");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    PostProperties(String value, Class<?> type, String defaultValue) {
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
