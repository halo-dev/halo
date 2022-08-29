package run.halo.app.theme;

/**
 * @author guqing
 * @since 2.0.0
 */
public enum DefaultTemplateEnum {
    CATEGORIES("categories"),

    ARCHIVES("posts"),

    POST("post"),

    TAGS("tags");

    private final String value;

    DefaultTemplateEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
