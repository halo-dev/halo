package run.halo.app.model.properties;

/**
 * Qi niu yun oss properties.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-26
 */
public enum QnYunProperties implements PropertyEnum {

    /**
     * Qiniu yun oss zone.
     */
    OSS_ZONE("oss_qiniu_zone", String.class, "auto"),

    /**
     * Qiniu yun oss access key.
     */
    OSS_ACCESS_KEY("oss_qiniu_access_key", String.class, ""),

    /**
     * Qiniu yun oss secret key.
     */
    OSS_SECRET_KEY("oss_qiniu_secret_key", String.class, ""),

    /**
     * Qiniu yun oss domain protocol.
     */
    OSS_PROTOCOL("oss_qiniu_domain_protocol", String.class, "https://"),

    /**
     * Qiniu yun oss domain.
     */
    OSS_DOMAIN("oss_qiniu_domain", String.class, ""),

    /**
     * Qiniu yun oss bucket.
     */
    OSS_BUCKET("oss_qiniu_bucket", String.class, ""),

    /**
     * Qiniu yun oss style rule.
     */
    OSS_STYLE_RULE("oss_qiniu_style_rule", String.class, ""),

    /**
     * Qiniu yun oss thumbnail style rule.
     */
    OSS_THUMBNAIL_STYLE_RULE("oss_qiniu_thumbnail_style_rule", String.class, "");

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
