package run.halo.app.model.properties;

/**
 * Tencent yun cos properties.
 *
 * @author wangya
 * @author ryanwang
 * @date 2019-07-25
 */
public enum TencentYunProperties implements PropertyEnum {

    /**
     * Tencent yun cos domain protocol.
     */
    COS_PROTOCOL("cos_tencentyun_domain_protocol", String.class, "https://"),

    /**
     * Tencent yun cos domain.
     */
    COS_DOMAIN("cos_tencentyun_domain", String.class, ""),

    /**
     * Tencent yun cos endpoint.
     */
    COS_REGION("cos_tencentyun_region", String.class, ""),

    /**
     * Tencent yun cos bucket name.
     */
    COS_BUCKET_NAME("cos_tencentyun_bucket_name", String.class, ""),

    /**
     * Tencent yun cos secret id.
     */
    COS_SECRET_ID("cos_tencentyun_secret_id", String.class, ""),

    /**
     * Tencent yun cos secret key.
     */
    COS_SECRET_KEY("cos_tencentyun_secret_key", String.class, ""),

    /**
     * Tencent yun cos style rule.
     */
    COS_STYLE_RULE("cos_tencentyun_style_rule", String.class, ""),

    /**
     * Tencent yun cos thumbnail style rule.
     */
    COS_THUMBNAIL_STYLE_RULE("cos_tencentyun_thumbnail_style_rule", String.class, "");

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
