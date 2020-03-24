package run.halo.app.model.properties;

import run.halo.app.model.support.HaloConst;

/**
 * Upyun oss properties.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-27
 */
public enum UpOssProperties implements PropertyEnum {

    /**
     * upyun oss source
     */
    OSS_SOURCE("oss_upyun_source", String.class, ""),

    /**
     * upyun oss password
     */
    OSS_PASSWORD("oss_upyun_password", String.class, ""),

    /**
     * upyun oss bucket
     */
    OSS_BUCKET("oss_upyun_bucket", String.class, ""),

    /**
     * upyun oss domain protocol
     */
    OSS_PROTOCOL("oss_upyun_domain_protocol", String.class, HaloConst.PROTOCOL_HTTPS),

    /**
     * upyun oss domain
     */
    OSS_DOMAIN("oss_upyun_domain", String.class, ""),

    /**
     * upyun oss operator
     */
    OSS_OPERATOR("oss_upyun_operator", String.class, ""),

    /**
     * upyun oss style rule
     */
    OSS_STYLE_RULE("oss_upyun_style_rule", String.class, ""),

    /**
     * upyun oss thumbnail style rule
     */
    OSS_THUMBNAIL_STYLE_RULE("oss_upyun_thumbnail_style_rule", String.class, "");

    private final String defaultValue;
    private final String value;
    private final Class<?> type;

    UpOssProperties(String value, Class<?> type, String defaultValue) {
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
