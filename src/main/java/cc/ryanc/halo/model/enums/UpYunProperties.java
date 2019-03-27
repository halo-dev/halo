package cc.ryanc.halo.model.enums;

/**
 * You pai yun properties.
 *
 * @author johnniang
 * @date 3/27/19
 */
public enum UpYunProperties implements PropertyEnum {

    OSS_SOURCE("upyun_oss_source", String.class),
    OSS_PASSWORD("upyun_oss_password", String.class),
    OSS_BUCKET("upyun_oss_bucket", String.class),
    OSS_DOMAIN("upyun_oss_domain", String.class),
    OSS_OPERATOR("upyun_oss_operator", String.class),
    OSS_SMALL_URL("ypyun_oss_small_url", String.class);

    private String value;

    private Class<?> type;

    UpYunProperties(String value, Class<?> type) {
        this.value = value;
        this.type = type;
    }


    @Override
    public Class<?> getType() {
        return null;
    }

    @Override
    public String getValue() {
        return null;
    }
}
