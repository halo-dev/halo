package run.halo.app.model.properties;

/**
 * Comment properties.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-01
 */
public enum CommentProperties implements PropertyEnum {

    /**
     * The default gravatar type.
     */
    GRAVATAR_DEFAULT("comment_gravatar_default", String.class, "mm"),

    /**
     * Does it need to be checked.
     */
    NEW_NEED_CHECK("comment_new_need_check", Boolean.class, "true"),

    /**
     * New mail notification.
     */
    NEW_NOTICE("comment_new_notice", Boolean.class, "false"),

    /**
     * Reply to the email to notify the author.
     */
    REPLY_NOTICE("comment_reply_notice", Boolean.class, "false"),

    /**
     * Whether to enable comment api.
     */
    API_ENABLED("comment_api_enabled", Boolean.class, "true"),

    /**
     * Comment list page size.
     */
    PAGE_SIZE("comment_page_size", Integer.class, "10"),

    /**
     * Placeholder for comment content input.
     */
    CONTENT_PLACEHOLDER("comment_content_placeholder", String.class, ""),

    /**
     * Dependent comment js.
     */
    INTERNAL_PLUGIN_JS("comment_internal_plugin_js", String.class,
        "//cdn.jsdelivr.net/npm/halo-comment@latest/dist/halo-comment.min.js"),

    COMMENT_BAN_TIME("comment_ban_time", Integer.class, "10"),

    COMMENT_RANGE("comment_range", Integer.class, "30");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    CommentProperties(String value, Class<?> type, String defaultValue) {
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
