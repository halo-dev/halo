package run.halo.app.model.properties;

/**
 * You pai yun properties.
 *
 * @author johnniang
 * @date 3/27/19
 */
public enum UpYunProperties implements PropertyEnum {

    OSS_SOURCE("oss_upyun_source", String.class, ""),

    OSS_PASSWORD("oss_upyun_password", String.class, ""),

    OSS_BUCKET("oss_upyun_bucket", String.class, ""),

    OSS_DOMAIN("oss_upyun_domain", String.class, ""),

    OSS_OPERATOR("oss_upyun_operator", String.class, ""),

    OSS_SMALL_URL("oss_upyun_small_url", String.class, "");

    private String value;

    private Class<?> type;

    private final String defaultValue;

    UpYunProperties(String value, Class<?> type, String defaultValue) {
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
