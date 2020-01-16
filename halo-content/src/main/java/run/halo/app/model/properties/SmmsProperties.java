package run.halo.app.model.properties;

/**
 * Base meta entity.
 *
 * @author ryanwang
 * @author ikaisec
 * @date 2019-08-04
 */
public enum SmmsProperties implements PropertyEnum {

    /**
     * SM.MS personal api secret token
     */
    SMMS_API_SECRET_TOKEN("smms_api_secret_token", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    SmmsProperties(String value, Class<?> type, String defaultValue) {
        this.defaultValue = defaultValue;
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
    public String defaultValue() {
        return defaultValue;
    }

    @Override
    public String getValue() {
        return value;
    }
}
