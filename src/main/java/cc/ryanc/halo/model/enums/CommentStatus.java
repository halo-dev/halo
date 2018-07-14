package cc.ryanc.halo.model.enums;

/**
 * @author : RYAN0UP
 * @date : 2018/7/1
 */
public enum CommentStatus {

    /**
     * 已发布
     */
    PUBLISHED(0,"已发布"),

    /**
     * 待审核
     */
    CHECKING(1,"待审核"),

    /**
     * 回收站
     */
    RECYCLE(2,"回收站");

    private Integer code;
    private String desc;

    CommentStatus(Integer code, String desc) {
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
