package run.halo.app.model.params;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Parameters required to send the reset password verification code.
 *
 * @author ryanwang
 * @date 2022-01-24
 */
@Data
public class ResetPasswordSendCodeParam {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    private String email;
}
