package cc.ryanc.halo.model.enums;

/**
 * <pre>
 *     评论状态enum
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/7/1
 */
public enum CommentStatusEnum {

    /**
     * 已发布
     */
    PUBLISHED(0, "已发布"),

    /**
     * 待审核
     */
    CHECKING(1, "待审核"),

    /**
     * 回收站
     */
    RECYCLE(2, "回收站");

    private Integer code;
    private String desc;

    CommentStatusEnum(Integer code, String desc) {
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
