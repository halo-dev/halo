package run.halo.app.infra;

import lombok.Data;

/**
 * @author guqing
 * @since 2.0.0
 */
public enum SystemSetting {
    THEME("theme", Theme.class);

    private final String group;
    private final Class<?> valueType;

    SystemSetting(String group, Class<?> valueType) {
        this.group = group;
        this.valueType = valueType;
    }

    public String getGroup() {
        return group;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    @Data
    public static class Theme {
        private String active;
    }
}
