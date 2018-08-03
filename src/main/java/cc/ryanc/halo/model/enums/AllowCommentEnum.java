package cc.ryanc.halo.model.enums;

/**
 * <pre>
 *     文章是否允许评论enum
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/7/9
 */
public enum AllowCommentEnum {
    /**
     * 允许评论
     */
    ALLOW(1),

    /**
     * 不允许评论
     */
    DISALLOW(0);

    private Integer code;

    AllowCommentEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
