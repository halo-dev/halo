package run.halo.app.model.properties;

/**
 * BaiDuYun properties.
 *
 * @author wangya
 * @author ryanwang
 * @date 2019-07-19
 */
public enum BaiDuYunProperties implements PropertyEnum {

    /**
     * Baidu yun bos domain.
     */
    BOS_DOMAIN("bos_baiduyun_domain",String.class,""),

    /**
     * Baidu yun bos endpoint.
     */
    BOS_ENDPOINT("bos_baiduyun_endpoint", String.class, ""),

    /**
     * Baidu yun bos bucket name.
     */
    BOS_BUCKET_NAME("bos_baiduyun_bucket_name", String.class, ""),

    /**
     * Baidu yun bos access key.
     */
    BOS_ACCESS_KEY("bos_baiduyun_access_key", String.class, ""),

    /**
     * Baidu yun bos secret key.
     */
    BOS_SECRET_KEY("bos_baiduyun_secret_key", String.class, ""),

    /**
     * Baidu yun bos style rule.
     */
    BOS_STYLE_RULE("bos_baiduyun_style_rule", String.class, ""),

    /**
     * Baidu yun bos thumbnail style rule.
     */
    BOS_THUMBNAIL_STYLE_RULE("bos_baiduyun_thumbnail_style_rule", String.class, "");

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
