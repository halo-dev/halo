package run.halo.app.model.properties;

/**
 * TencentYun properties.
 *
 * @author wangya
 * @author ryanwang
 * @date 2019-07-25
 */
public enum TencentYunProperties implements PropertyEnum {

    /**
     * Tencentyun cos domain.
     */
    COS_DOMAIN("cos_tencentyun_domain",String.class,""),

    /**
     * Tencentyun cos endpoint.
     */
    COS_REGION("cos_tencentyun_region", String.class, ""),

    /**
     * Tencentyun cos bucket name.
     */
    COS_BUCKET_NAME("cos_tencentyun_bucket_name", String.class, ""),

    /**
     * Tencentyun cos secret id.
     */
    COS_SECRET_ID("cos_tencentyun_secret_id", String.class, ""),

    /**
     * Tencentyun cos secret key.
     */
    COS_SECRET_KEY("cos_tencentyun_secret_key", String.class, "");

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
