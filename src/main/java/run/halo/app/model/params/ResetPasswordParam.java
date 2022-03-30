package run.halo.app.model.params;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Reset password params.
 *
 * @author ryanwang
 * @date 2019-09-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResetPasswordParam extends ResetPasswordSendCodeParam {

    @NotBlank(message = "验证码不能为空")
    private String code;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 100, message = "密码的字符长度必须在 {min} - {max} 之间")
    private String password;
}
