package run.halo.app.model.properties;

import run.halo.app.model.support.HaloConst;

/**
 * Baidu bos properties.
 *
 * @author wangya
 * @author ryanwang
 * @date 2019-07-19
 */
public enum BaiduBosProperties implements PropertyEnum {

    /**
     * Baidu bos domain protocol.
     */
    BOS_PROTOCOL("bos_baidu_domain_protocol", String.class, HaloConst.PROTOCOL_HTTPS),

    /**
     * Baidu bos domain.
     */
    BOS_DOMAIN("bos_baidu_domain", String.class, ""),

    /**
     * Baidu bos endpoint.
     */
    BOS_ENDPOINT("bos_baidu_endpoint", String.class, ""),

    /**
     * Baidu bos bucket name.
     */
    BOS_BUCKET_NAME("bos_baidu_bucket_name", String.class, ""),

    /**
     * Baidu bos access key.
     */
    BOS_ACCESS_KEY("bos_baidu_access_key", String.class, ""),

    /**
     * Baidu bos secret key.
     */
    BOS_SECRET_KEY("bos_baidu_secret_key", String.class, ""),

    /**
     * Baidu bos style rule.
     */
    BOS_STYLE_RULE("bos_baidu_style_rule", String.class, ""),

    /**
     * Baidu bos thumbnail style rule.
     */
    BOS_THUMBNAIL_STYLE_RULE("bos_baidu_thumbnail_style_rule", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    BaiduBosProperties(String value, Class<?> type, String defaultValue) {
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
