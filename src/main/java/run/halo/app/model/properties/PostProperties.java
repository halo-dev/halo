package run.halo.app.model.properties;

import run.halo.app.model.enums.TimeUnit;

/**
 * Post properties.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-01
 */
public enum PostProperties implements PropertyEnum {

    /**
     * Post summary words length.
     */
    SUMMARY_LENGTH("post_summary_length", Integer.class, "150"),

    /**
     * Rss page size.
     */
    RSS_PAGE_SIZE("rss_page_size", Integer.class, "20"),

    /**
     * Rss content type,full or summary.
     */
    RSS_CONTENT_TYPE("rss_content_type", Integer.class, "full"),

    /**
     * Post index page size.
     */
    INDEX_PAGE_SIZE("post_index_page_size", Integer.class, "10"),

    /**
     * Archives page size.
     */
    ARCHIVES_PAGE_SIZE("post_archives_page_size", Integer.class, "10"),

    /**
     * Post index sort.
     */
    INDEX_SORT("post_index_sort", String.class, "createTime"),

    /**
     * Enable auto cleaning recycled post.
     */
    RECYCLED_POST_CLEANING_ENABLED("recycled_post_cleaning_enabled", Boolean.class, "false"),

    /**
     * Recycled post retention time
     */
    RECYCLED_POST_RETENTION_TIME("recycled_post_retention_time", Integer.class, "30"),

    /**
     * Recycled post retention time unit.
     */
    RECYCLED_POST_RETENTION_TIMEUNIT("recycled_post_retention_timeunit", TimeUnit.class,
        TimeUnit.DAY.name());

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
