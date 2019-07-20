package run.halo.app.model.properties;

/**
 * BaiDuYun properties.
 *
 * @author wangya
 * @date 2019-07-19
 */
public enum BaiDuYunProperties implements PropertyEnum {

    /**
     * BaiDuyun oss endpoint.
     */
    OSS_ENDPOINT("oss_baiduyun_endpoint", String.class, ""),

    /**
     * BaiDuyun oss bucket name.
     */
    OSS_BUCKET_NAME("oss_baiduyun_bucket_name", String.class, ""),

    /**
     * BaiDuyun oss access key.
     */
    OSS_ACCESS_KEY("oss_baiduyun_access_key", String.class, ""),

    /**
     * BaiDuyun oss access secret.
     */
    OSS_ACCESS_SECRET("oss_baiduyun_access_secret", String.class, ""),

    /**
     * BaiDuyun oss style rule
     */
    OSS_STYLE_RULE("oss_baiduyun_style_rule", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    BaiDuYunProperties(String value, Class<?> type, String defaultValue) {
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
