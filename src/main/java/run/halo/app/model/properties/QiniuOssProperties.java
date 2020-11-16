package run.halo.app.model.properties;

import run.halo.app.model.support.HaloConst;

/**
 * Qiniu oss properties.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-26
 */
public enum QiniuOssProperties implements PropertyEnum {

    /**
     * Qiniu oss zone.
     */
    OSS_ZONE("oss_qiniu_zone", String.class, "auto"),

    /**
     * Qiniu oss access key.
     */
    OSS_ACCESS_KEY("oss_qiniu_access_key", String.class, ""),

    /**
     * Qiniu oss secret key.
     */
    OSS_SECRET_KEY("oss_qiniu_secret_key", String.class, ""),

    /**
     * Qiniu oss source
     */
    OSS_SOURCE("oss_qiniu_source", String.class, ""),

    /**
     * Qiniu oss domain protocol.
     */
    OSS_PROTOCOL("oss_qiniu_domain_protocol", String.class, HaloConst.PROTOCOL_HTTPS),

    /**
     * Qiniu oss domain.
     */
    OSS_DOMAIN("oss_qiniu_domain", String.class, ""),

    /**
     * Qiniu oss bucket.
     */
    OSS_BUCKET("oss_qiniu_bucket", String.class, ""),

    /**
     * Qiniu oss style rule.
     */
    OSS_STYLE_RULE("oss_qiniu_style_rule", String.class, ""),

    /**
     * Qiniu oss thumbnail style rule.
     */
    OSS_THUMBNAIL_STYLE_RULE("oss_qiniu_thumbnail_style_rule", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    QiniuOssProperties(String value, Class<?> type, String defaultValue) {
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
