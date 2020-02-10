package run.halo.app.model.properties;

/**
 * Comment properties.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-01
 */
public enum CommentProperties implements PropertyEnum {

    GRAVATAR_DEFAULT("comment_gravatar_default", String.class, "mm"),

    NEW_NEED_CHECK("comment_new_need_check", Boolean.class, "true"),

    NEW_NOTICE("comment_new_notice", Boolean.class, "false"),

    PASS_NOTICE("comment_pass_notice", Boolean.class, "false"),

    REPLY_NOTICE("comment_reply_notice", Boolean.class, "false"),

    API_ENABLED("comment_api_enabled", Boolean.class, "true"),

    PAGE_SIZE("comment_page_size", Integer.class, "10"),

    CONTENT_PLACEHOLDER("comment_content_placeholder", String.class, ""),

    INTERNAL_PLUGIN_JS("comment_internal_plugin_js", String.class, "//cdn.jsdelivr.net/gh/halo-dev/halo-comment@latest/dist/halo-comment.min.js"),

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
