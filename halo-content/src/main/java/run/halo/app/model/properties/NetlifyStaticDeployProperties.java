package run.halo.app.model.properties;

/**
 * Netlify static deploy properties.
 *
 * @author ryanwang
 * @date 2019-12-26
 */
public enum NetlifyStaticDeployProperties implements PropertyEnum {

    /**
     * Netlify static deploy domain.
     */
    NETLIFY_DOMAIN("netlify_static_deploy_domain", String.class, ""),

    /**
     * Netlify static deploy site id.
     */
    NETLIFY_SITE_ID("netlify_static_deploy_site_id", String.class, ""),

    /**
     * Netlify static deploy token.
     */
    NETLIFY_TOKEN("netlify_static_deploy_token", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    NetlifyStaticDeployProperties(String value, Class<?> type, String defaultValue) {
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
