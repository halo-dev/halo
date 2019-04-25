package run.halo.app.model.properties;

/**
 * Qi niu yun properties.
 *
 * @author johnniang
 * @date 3/26/19
 */
public enum QnYunProperties implements PropertyEnum {

    ZONE("oss_qiniu_zone", String.class, "auto"),

    ACCESS_KEY("oss_qiniu_access_key", String.class, ""),

    SECRET_KEY("oss_qiniu_secret_key", String.class, ""),

    DOMAIN("oss_qiniu_domain", String.class, ""),

    BUCKET("oss_qiniu_bucket", String.class, ""),

    SMALL_URL("oss_qiniu_small_url", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    QnYunProperties(String value, Class<?> type, String defaultValue) {
        this.defaultValue = defaultValue;
        if (!PropertyEnum.isSupportedType(type)) {
            throw new IllegalArgumentException("Unsupported blog property type: " + type);
        }

        this.value = value;
        this.type = type;
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
