package run.halo.app.model.params;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * User password param.
 *
 * @author johnniang
 * @date 3/26/19
 */
@Data
public class PasswordParam {

    @NotBlank(message = "旧密码不能为空")
    @Size(max = 100, message = "密码的字符长度不能超过 {max}")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(max = 100, message = "密码的字符长度不能超过 {max}")
    private String newPassword;

}
