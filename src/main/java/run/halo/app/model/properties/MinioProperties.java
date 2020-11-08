package run.halo.app.model.properties;

/**
 * Minio properties.
 *
 * @author Wh1te
 * @date 2020-10-03
 */
public enum MinioProperties implements PropertyEnum {

    /**
     * Minio endpoint.
     */
    ENDPOINT("minio_endpoint", String.class, ""),

    /**
     * Minio bucket name.
     */
    BUCKET_NAME("minio_bucket_name", String.class, ""),

    /**
     * Minio access key.
     */
    ACCESS_KEY("minio_access_key", String.class, ""),

    /**
     * Minio access secret.
     */
    ACCESS_SECRET("minio_access_secret", String.class, ""),

    /**
     * Minio source
     */
    SOURCE("minio_source", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    MinioProperties(String value, Class<?> type, String defaultValue) {
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
