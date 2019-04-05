package run.halo.app.model.properties;

/**
 * AliYun properties.
 * @author MyFaith
 * @date 2019-04-04 00:00:56
 */
public enum  AliYunProperties implements PropertyEnum {
    OSS_ENDPOINT("oss_aliyun_endpoint", String.class),
    OSS_BUCKET_NAME("oss_aliyun_bucket_name", String.class),
    OSS_ACCESS_KEY("oss_aliyun_access_key", String.class),
    OSS_ACCESS_SECRET("oss_aliyun_access_secret", String.class);

    private String value;
    private Class<?> type;

    AliYunProperties(String value, Class<?> type) {
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
    public String getValue() {
        return value;
    }
}
