package cc.ryanc.halo.model.enums;

/**
 * Email properties.
 *
 * @author johnniang
 * @date 3/27/19
 */
public enum EmailProperties implements PropertyEnum {

    SMTP_HOST("email_smtp_host", String.class),
    SMTP_USERNAME("email_smtp_username", String.class),
    SMTP_PASSWORD("email_smtp_password", String.class),
    FROM_NAME("email_from_name", String.class),
    ENABLED("email_enabled", Boolean.class),
    COMMENT_REPLY_NOTICE_ENABLED("email_comment_reply_notice_enabled", Boolean.class),
    NEW_COMMENT_NOTICE_ENABLED("email_new_comment_notice_enabled", Boolean.class),
    COMMENT_PASS_NOTICE_ENABLED("email_comment_pass_notice_enabled", Boolean.class);

    private final String value;

    private final Class<?> type;

    EmailProperties(String value, Class<?> type) {
        if (!PropertyEnum.isSupportedType(type)) {
            throw new IllegalArgumentException("Unsupported blog property type: " + type);
        }

        this.value = value;
        this.type = type;
    }

    @Override
    public Class<?> getType() {
        return null;
    }

    @Override
    public String getValue() {
        return null;
    }}
