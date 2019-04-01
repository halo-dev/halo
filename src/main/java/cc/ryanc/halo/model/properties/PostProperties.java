package cc.ryanc.halo.model.properties;

/**
 * @author johnniang
 * @date 4/1/19
 */
public enum PostProperties implements PropertyEnum {

    SUMMARY_LENGTH("post_summary_length", Integer.class),

    RSS_PAGE_SIZE("rss_page_size", Integer.class),

    INDEX_PAGE_SIZE("post_index_page_size", Integer.class),
    ;

    private final String value;

    private final Class<?> type;

    PostProperties(String value, Class<?> type) {
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
