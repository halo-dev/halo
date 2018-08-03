package cc.ryanc.halo.model.enums;

/**
 * <pre>
 *     常用数字
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/8/1
 */
public enum CommonParamsEnum {

    /**
     * 数字10
     */
    TEN(10),

    /**
     * 数字5
     */
    FIVE(5),

    /**
     * 数字404
     */
    NOT_FOUND(404),

    /**
     * 数字1024
     */
    BYTE(1024);

    private Integer value;

    CommonParamsEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
