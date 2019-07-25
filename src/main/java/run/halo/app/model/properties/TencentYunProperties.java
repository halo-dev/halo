package run.halo.app.model.properties;

/**
 * TencentYun properties.
 *
 * @author wangya
 * @date 2019-07-25
 */
public enum TencentYunProperties implements PropertyEnum {

    /**
     * Tencentyun oss endpoint.
     */
    OSS_REGION("oss_tencentyun_region", String.class, ""),

    /**
     * Tencentyun oss bucket name.
     */
    OSS_BUCKET_NAME("oss_tencentyun_bucket_name", String.class, ""),

    /**
     * Tencentyun oss access key.
     */
    OSS_ACCESS_KEY("oss_tencentyun_access_key", String.class, ""),

    /**
     * Tencentyun oss access secret.
     */
    OSS_ACCESS_SECRET("oss_tencentyun_access_secret", String.class, ""),

    /**
     * Tencentyun oss style rule
     */
    OSS_STYLE_RULE("oss_tencentyun_style_rule", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    TencentYunProperties(String value, Class<?> type, String defaultValue) {
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
