package cc.ryanc.halo.model.enums;

/**
 * @author : RYAN0UP
 * @date : 2018/7/16
 */
public enum TrueFalse {

    /**
     * 真
     */
    TRUE("true"),

    /**
     * 假
     */
    FALSE("false");

    private String desc;

    TrueFalse(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
