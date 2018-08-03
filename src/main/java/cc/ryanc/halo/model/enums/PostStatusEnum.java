package cc.ryanc.halo.model.enums;

/**
 * <pre>
 *     文章状态enum
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/7/1
 */
public enum PostStatusEnum {

    /**
     * 已发布
     */
    PUBLISHED(0, "已发布"),

    /**
     * 草稿
     */
    DRAFT(1, "草稿"),

    /**
     * 回收站
     */
    RECYCLE(2, "回收站");

    private Integer code;
    private String desc;

    PostStatusEnum(Integer code, String desc) {
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
