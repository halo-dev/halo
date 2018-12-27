package cc.ryanc.halo.model.enums;

/**
 * <pre>
 *     附件存储地址enum
 * </pre>
 *
 * @author : Yawn
 * @date : 2018/12/4
 */
public enum AttachLocationEnum {

    /**
     * 服务器
     */
    SERVER(0,"server"),

    /**
     * 七牛
     */
    QINIU(1,"qiniu"),

    /**
     * 又拍云
     */
    UPYUN(2,"upyun");

    private Integer code;
    private String desc;

    AttachLocationEnum(Integer code, String desc) {
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
