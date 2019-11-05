package run.halo.app.model.properties;

/**
 * AliYun properties.
 *
 * @author MyFaith
 * @author ryanwang
 * @date 2019-04-04 00:00:56
 */
public enum AliYunProperties implements PropertyEnum {

    /**
     * Aliyun oss domain
     */
    OSS_DOMAIN("oss_aliyun_domain",String.class,""),

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
    OSS_ACCESS_SECRET("oss_aliyun_access_secret", String.class, ""),

    /**
     * Aliyun oss style rule
     */
    OSS_STYLE_RULE("oss_aliyun_style_rule", String.class, ""),

    /**
     * Aliyun oss thumbnail style rule
     */
    OSS_THUMBNAIL_STYLE_RULE("oss_aliyun_thumbnail_style_rule", String.class, "");

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
