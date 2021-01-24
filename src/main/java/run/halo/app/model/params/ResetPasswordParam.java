package run.halo.app.model.params;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Reset password params.
 *
 * @author ryanwang
 * @date 2019-09-05
 */
@Data
public class ResetPasswordParam {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    private String email;

    private String code;

    private String password;
}
