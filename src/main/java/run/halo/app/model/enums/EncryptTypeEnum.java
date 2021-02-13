package run.halo.app.model.enums;

/**
 * @author zhixiang.yuan
 * @since 2021/01/24 10:45:33
 */
public enum EncryptTypeEnum {

    POST("post"), CATEGORY("category");

    private final String name;

    EncryptTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
