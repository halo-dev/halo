package run.halo.app.model.properties;

/**
 * @author : RYAN0UP
 * @date : 2019-03-17
 */
public enum BlogProperties implements PropertyEnum {

    /**
     * Blog locale.
     */
    BLOG_LOCALE("blog_locale", String.class),

    /**
     * Blog title.
     */
    BLOG_TITLE("blog_title", String.class),

    /**
     * Blog logo.
     */
    BLOG_LOGO("blog_logo", String.class),

    /**
     * Blog url.
     */
    BLOG_URL("blog_url", String.class),

    /**
     * Blog favicon.
     */
    BLOG_FAVICON("blog_favicon", String.class),

    /**
     * Blog footer info.
     */
    BLOG_FOOTER_INFO("blog_footer_info", String.class);

    private String value;

    private Class<?> type;

    BlogProperties(String value, Class<?> type) {
        if (!PropertyEnum.isSupportedType(type)) {
            throw new IllegalArgumentException("Unsupported blog property type: " + type);
        }

        this.value = value;
        this.type = type;
    }

    /**
     * Get enum value.
     *
     * @return enum value
     */
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

}
