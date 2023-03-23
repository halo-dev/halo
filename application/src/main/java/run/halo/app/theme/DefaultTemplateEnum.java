package run.halo.app.theme;

/**
 * @author guqing
 * @since 2.0.0
 */
public enum DefaultTemplateEnum {
    INDEX("index"),

    CATEGORIES("categories"),

    CATEGORY("category"),

    ARCHIVES("archives"),

    POST("post"),

    TAG("tag"),

    TAGS("tags"),

    SINGLE_PAGE("page"),

    AUTHOR("author");

    private final String value;

    DefaultTemplateEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DefaultTemplateEnum convertFrom(String template) {
        for (DefaultTemplateEnum e : values()) {
            if (e.getValue().equals(template)) {
                return e;
            }
        }
        return null;
    }
}
