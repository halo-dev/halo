package run.halo.app.model.enums;

import lombok.Getter;

/**
 * 评论违规类型枚举
 *
 * @author Lei XinXin
 * @date 2020/1/4
 */
@Getter
public enum CommentViolationTypeEnum {
    /**
     * 评论违规类型
     */
    NORMAL(0),
    /**
     * 频繁
     */
    FREQUENTLY(1);

    private final int type;

    CommentViolationTypeEnum(int type) {
        this.type = type;
    }
}
