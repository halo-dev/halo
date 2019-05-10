package run.halo.app.model.params;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * User password param.
 *
 * @author johnniang
 * @date 3/26/19
 */
@Data
public class PasswordParam {

    @NotBlank(message = "Old password must not be blank")
    @Size(max = 100, message = "Length of password must not be more than {max}")
    private String oldPassword;

    @NotBlank(message = "New password must not be blank")
    @Size(max = 100, message = "Length of password must not be more than {max}")
    private String newPassword;

}
