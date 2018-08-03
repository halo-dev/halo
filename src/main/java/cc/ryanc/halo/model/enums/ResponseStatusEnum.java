package cc.ryanc.halo.model.enums;

/**
 * <pre>
 *     请求响应enum
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/7/1
 */
public enum ResponseStatusEnum {

    /**
     * 请求成功
     */
    SUCCESS(200, "OK"),

    /**
     * 资源为空
     */
    EMPTY(204, "No Content"),

    /**
     * 服务器内部错误
     */
    ERROR(500, "Internal Server Error"),

    /**
     * 未找到资源
     */
    NOTFOUND(404, "Not Found");

    private Integer code;
    private String msg;

    ResponseStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
