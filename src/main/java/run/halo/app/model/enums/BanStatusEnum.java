package run.halo.app.model.enums;

import lombok.Getter;

/**
 * 封禁状态
 *
 * @author Lei XinXin
 * @date 2020/1/5
 */
@Getter
public enum BanStatusEnum {
    /**
     * 封禁状态
     */
    NORMAL(0);

    private final int status;

    BanStatusEnum(int status) {
        this.status = status;
    }
}
