package run.halo.app.model.properties;

/**
 * Comment properties.
 *
 * @author johnniang
 * @date 4/1/19
 */
public enum CommentProperties implements PropertyEnum {

    GAVATAR_DEFAULT("comment_gavatar_default", String.class),

    NEW_NEED_CHECK("comment_new_need_check", Boolean.class),

    NEW_NOTICE("comment_new_notice", Boolean.class),

    PASS_NOTICE("comment_pass_notice", Boolean.class),

    REPLY_NOTICE("comment_reply_notice", Boolean.class),

    API_ENABLED("comment_api_enabled", Boolean.class),

    PAGE_SIZE("comment_page_size", Integer.class),

    CONTENT_PLACEHOLDER("comment_content_placeholder", String.class),

    CUSTOM_STYLE("comment_custom_style", String.class);

    private final String value;

    private final Class<?> type;

    CommentProperties(String value, Class<?> type) {
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
    }}
