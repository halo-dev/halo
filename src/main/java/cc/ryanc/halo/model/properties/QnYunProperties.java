package cc.ryanc.halo.model.properties;

/**
 * Qi niu yun properties.
 *
 * @author johnniang
 * @date 3/26/19
 */
public enum QnYunProperties implements PropertyEnum {

    ZONE("oss_qiniu_zone", String.class),

    ACCESS_KEY("oss_qiniu_access_key", String.class),

    SECRET_KEY("oss_qiniu_secret_key", String.class),

    DOMAIN("oss_qiniu_domain", String.class),

    BUCKET("oss_qiniu_bucket", String.class),

    SMALL_URL("oss_qiniu_small_url", String.class);

    private final String value;

    private final Class<?> type;

    QnYunProperties(String value, Class<?> type) {
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

}
