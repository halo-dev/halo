package run.halo.app.model.properties;

/**
 * Sheet properties.
 *
 * @author ryanwang
 * @date 2020-02-11
 */
public enum SheetProperties implements PropertyEnum {

    /**
     * Links page title.
     */
    LINKS_TITLE("links_title", String.class, "友情链接"),

    /**
     * Photos page title.
     */
    PHOTOS_TITLE("photos_title", String.class, "图库"),

    /**
     * Photos page size.
     */
    PHOTOS_PAGE_SIZE("photos_page_size", Integer.class, "10"),

    /**
     * Journals page title.
     */
    JOURNALS_TITLE("journals_title", String.class, "日志"),

    /**
     * Journals page size.
     */
    JOURNALS_PAGE_SIZE("journals_page_size", Integer.class, "10");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    SheetProperties(String value, Class<?> type, String defaultValue) {
        this.value = value;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }
}
