package cc.ryanc.halo.model.enums;

/**
 * @author : RYAN0UP
 * @date : 2018/7/1
 */
public enum PostStatus {

    /**
     * 已发布
     */
    PUBLISHED(0,"已发布"),

    /**
     * 草稿
     */
    DRAFT(1,"草稿"),

    /**
     * 回收站
     */
    RECYCLE(2,"回收站");

    private Integer code;
    private String desc;

    PostStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
