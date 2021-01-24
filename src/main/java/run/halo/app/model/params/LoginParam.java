package run.halo.app.model.params;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * Login param.
 *
 * @author johnniang
 * @date 3/28/19
 */
@Data
@ToString
public class LoginParam {

    @NotBlank(message = "用户名或邮箱不能为空")
    @Size(max = 255, message = "用户名或邮箱的字符长度不能超过 {max}")
    private String username;

    @NotBlank(message = "登录密码不能为空")
    @Size(max = 100, message = "用户密码字符长度不能超过 {max}")
    private String password;

    @Size(min = 6, max = 6, message = "两步验证码应为 {max} 位")
    private String authcode;

}
