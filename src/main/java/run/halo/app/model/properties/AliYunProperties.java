package run.halo.app.model.properties;

/**
 * AliYun properties.
 *
 * @author MyFaith
 * @date 2019-04-04 00:00:56
 */
public enum AliYunProperties implements PropertyEnum {

    /**
     * Aliyun oss endpoint.
     */
    OSS_ENDPOINT("oss_aliyun_endpoint", String.class, ""),

    /**
     * Aliyun oss bucket name.
     */
    OSS_BUCKET_NAME("oss_aliyun_bucket_name", String.class, ""),

    /**
     * Aliyun oss access key.
     */
    OSS_ACCESS_KEY("oss_aliyun_access_key", String.class, ""),

    /**
     * Aliyun oss access secret.
     */
    OSS_ACCESS_SECRET("oss_aliyun_access_secret", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    AliYunProperties(String value, Class<?> type, String defaultValue) {
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
