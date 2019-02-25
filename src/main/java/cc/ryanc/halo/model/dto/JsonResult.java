package cc.ryanc.halo.model.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * <pre>
 *     Json格式
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/5/24
 */
@Data
public class JsonResult {

    /**
     * 返回的状态码 (Same as HttpStatus.value()).
     */
    private Integer code;

    /**
     * 返回信息
     */
    private String msg;

    /**
     * Development message.(Development environment only)
     */
    private String devMsg;

    /**
     * 返回的数据
     */
    private Object result;

    public JsonResult() {
    }

    /**
     * 只返回状态码
     *
     * @param code 状态码
     */
    public JsonResult(Integer code) {
        this.code = code;
    }

    /**
     * 不返回数据的构造方法
     *
     * @param code 状态码
     * @param msg  信息
     */
    public JsonResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 返回数据的构造方法
     *
     * @param code   状态码
     * @param msg    信息
     * @param result 数据
     */
    public JsonResult(Integer code, String msg, Object result) {
        this.code = code;
        this.msg = msg;
        this.result = result;
    }

    /**
     * 返回状态码和数据
     *
     * @param code   状态码
     * @param result 数据
     */
    public JsonResult(Integer code, Object result) {
        this.code = code;
        this.result = result;
    }

    public JsonResult(Integer code, String msg, String devMsg, Object result) {
        this.code = code;
        this.msg = msg;
        this.devMsg = devMsg;
        this.result = result;
    }

    /**
     * Create an ok result with message and data.
     *
     * @param data    result data
     * @param message result message
     * @return ok result with message and data
     */
    @NonNull
    public static JsonResult ok(@Nullable String message, @Nullable Object data) {
        return new JsonResult(HttpStatus.OK.value(), message, data);
    }

    /**
     * Creates an ok result with message only.
     *
     * @param message result message
     * @return ok result with message only
     */
    @NonNull
    public static JsonResult ok(@Nullable String message) {
        return ok(message, null);
    }
}
