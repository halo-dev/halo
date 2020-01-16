package run.halo.app.model.properties;

/**
 * Git static deploy properties.
 *
 * @author ryanwang
 * @date 2019-12-26
 */
public enum GitStaticDeployProperties implements PropertyEnum {

    /**
     * Git static deploy domain.
     */
    GIT_DOMAIN("git_static_deploy_domain", String.class, ""),

    /**
     * Git static deploy repository.
     */
    GIT_REPOSITORY("git_static_deploy_repository", String.class, ""),

    /**
     * Git static deploy branch.
     */
    GIT_BRANCH("git_static_deploy_branch", String.class, "master"),

    /**
     * Git static deploy username.
     */
    GIT_USERNAME("git_static_deploy_username", String.class, ""),

    /**
     * Git static deploy email.
     */
    GIT_EMAIL("git_static_deploy_email", String.class, ""),

    /**
     * Git static deploy token.
     */
    GIT_TOKEN("git_static_deploy_token", String.class, ""),

    /**
     * Git static deploy cname.
     */
    GIT_CNAME("git_static_deploy_cname", String.class, "");

    private final String value;

    private final Class<?> type;

    private final String defaultValue;

    GitStaticDeployProperties(String value, Class<?> type, String defaultValue) {
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
