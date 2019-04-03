package run.halo.app.model.properties;

/**
 * Email properties.
 *
 * @author johnniang
 * @date 3/27/19
 */
public enum EmailProperties implements PropertyEnum {

    HOST("email_host", String.class),

    PROTOCOL("email_protocol", String.class),

    SSL_PORT("email_ssl_port", Integer.class),

    USERNAME("email_username", String.class),

    PASSWORD("email_password", String.class),

    FROM_NAME("email_from_name", String.class),

    ENABLED("email_enabled", Boolean.class);

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
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }}
