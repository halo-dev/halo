package run.halo.app.model.properties;

import run.halo.app.model.enums.StaticDeployType;

/**
 * Static deploy properties.
 *
 * @author ryanwang
 * @date 2019-12-26
 */
public enum StaticDeployProperties implements PropertyEnum {

    /**
     * static deploy type
     */
    DEPLOY_TYPE("static_deploy_type", StaticDeployType.class, StaticDeployType.GIT.name());

    private final String value;

    private final Class<?> type;

    private final String defaultValue;


    StaticDeployProperties(String value, Class<?> type, String defaultValue) {
        this.value = value;
        this.type = type;
        this.defaultValue = defaultValue;
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
