package cc.ryanc.halo.model.enums;

/**
 * @author : RYAN0UP
 * @date : 2018/7/22
 */
public enum BackupType {

    /**
     * 资源文件
     */
    RESOURCES("resources"),

    /**
     * 数据库
     */
    DATABASES("databases"),

    /**
     * 文章
     */
    POSTS("posts");

    private String desc;

    BackupType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
