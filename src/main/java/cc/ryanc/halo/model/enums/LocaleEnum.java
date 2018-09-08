package cc.ryanc.halo.model.enums;

/**
 * @author : wangry
 * @version : 1.0
 * @date : 2018年09月08日
 */
public enum LocaleEnum {

    /**
     * 简体中文
     */
    ZH_CN("zh_CN"),

    /**
     * 英文
     */
    EN_US("en_US");

    private String value;

    LocaleEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
