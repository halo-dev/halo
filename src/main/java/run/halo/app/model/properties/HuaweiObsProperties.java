package run.halo.app.model.properties;

import run.halo.app.model.support.HaloConst;

/**
 * Huawei obs properties.
 *
 * @author qilin
 * @date 2020-04-03
 */
public enum HuaweiObsProperties implements PropertyEnum {

    /**
     * Huawei obs domain protocol
     */
    OSS_PROTOCOL("obs_huawei_domain_protocol", String.class, HaloConst.PROTOCOL_HTTPS),

    /**
     * Huawei obs domain
     */
    OSS_DOMAIN("obs_huawei_domain", String.class, ""),

    /**
     * Huawei obs endpoint.
     */
    OSS_ENDPOINT("obs_huawei_endpoint", String.class, ""),

    /**
     * Huawei obs bucket name.
     */
    OSS_BUCKET_NAME("obs_huawei_bucket_name", String.class, ""),

    /**
     * Huawei obs access key.
     */
    OSS_ACCESS_KEY("obs_huawei_access_key", String.class, ""),

    /**
     * Huawei obs access secret.
     */
    OSS_ACCESS_SECRET("obs_huawei_access_secret", String.class, ""),

    /**
     * Huawei obs source
     */
    OSS_SOURCE("obs_huawei_source", String.class, ""),

    /**
     * Huawei obs style rule.
     */
    OSS_STYLE_RULE("obs_huawei_style_rule", String.class, ""),

    /**
     * Huawei obs thumbnail style rule.
     */
    OSS_THUMBNAIL_STYLE_RULE("obs_huawei_thumbnail_style_rule", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    HuaweiObsProperties(String value, Class<?> type, String defaultValue) {
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
