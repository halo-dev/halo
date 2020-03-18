package run.halo.app.model.properties;

import run.halo.app.model.support.HaloConst;

/**
 * Ali yun oss properties.
 *
 * @author MyFaith
 * @author ryanwang
 * @date 2019-04-04
 */
public enum AliOssProperties implements PropertyEnum {

    /**
     * Aliyun oss domain protocol
     */
    OSS_PROTOCOL("oss_ali_domain_protocol", String.class, HaloConst.PROTOCOL_HTTPS),

    /**
     * Aliyun oss domain
     */
    OSS_DOMAIN("oss_ali_domain", String.class, ""),

    /**
     * Aliyun oss endpoint.
     */
    OSS_ENDPOINT("oss_ali_endpoint", String.class, ""),

    /**
     * Aliyun oss bucket name.
     */
    OSS_BUCKET_NAME("oss_ali_bucket_name", String.class, ""),

    /**
     * Aliyun oss access key.
     */
    OSS_ACCESS_KEY("oss_ali_access_key", String.class, ""),

    /**
     * Aliyun oss access secret.
     */
    OSS_ACCESS_SECRET("oss_ali_access_secret", String.class, ""),

    /**
     * Aliyun oss source
     */
    OSS_SOURCE("oss_ali_source", String.class, ""),

    /**
     * Aliyun oss style rule.
     */
    OSS_STYLE_RULE("oss_ali_style_rule", String.class, ""),

    /**
     * Aliyun oss thumbnail style rule.
     */
    OSS_THUMBNAIL_STYLE_RULE("oss_ali_thumbnail_style_rule", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    AliOssProperties(String value, Class<?> type, String defaultValue) {
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
