package run.halo.app.model.properties;

import run.halo.app.model.support.HaloConst;

/**
 * Tencent cos properties.
 *
 * @author wangya
 * @author ryanwang
 * @date 2019-07-25
 */
public enum TencentCosProperties implements PropertyEnum {

    /**
     * Tencent cos domain protocol.
     */
    COS_PROTOCOL("cos_tencent_domain_protocol", String.class, HaloConst.PROTOCOL_HTTPS),

    /**
     * Tencent cos domain.
     */
    COS_DOMAIN("cos_tencent_domain", String.class, ""),

    /**
     * Tencent cos endpoint.
     */
    COS_REGION("cos_tencent_region", String.class, ""),

    /**
     * Tencent cos bucket name.
     */
    COS_BUCKET_NAME("cos_tencent_bucket_name", String.class, ""),

    /**
     * Tencent cos secret id.
     */
    COS_SECRET_ID("cos_tencent_secret_id", String.class, ""),

    /**
     * Tencent cos secret key.
     */
    COS_SECRET_KEY("cos_tencent_secret_key", String.class, ""),

    /**
     * Tencent cos source
     */
    COS_SOURCE("cos_tencent_source", String.class, ""),

    /**
     * Tencent cos style rule.
     */
    COS_STYLE_RULE("cos_tencent_style_rule", String.class, ""),

    /**
     * Tencent cos thumbnail style rule.
     */
    COS_THUMBNAIL_STYLE_RULE("cos_tencent_thumbnail_style_rule", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    TencentCosProperties(String value, Class<?> type, String defaultValue) {
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
