package cc.ryanc.halo.model.properties;

/**
 * Qi niu yun properties.
 *
 * @author johnniang
 * @date 3/26/19
 */
public enum QnYunProperties implements PropertyEnum {

    ZONE("qiniu_zone", String.class),
    ACCESS_KEY("qiniu_access_key", String.class),
    SECRET_KEY("qiniu_secret_key", String.class),
    DOMAIN("qiniu_domain", String.class),
    BUCKET("qiniu_bucket", String.class),
    SMALL_URL("qiniu_small_url", String.class);

    private String value;

    private Class<?> type;

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
