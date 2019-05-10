package run.halo.app.model.params;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Login param.
 *
 * @author johnniang
 * @date 3/28/19
 */
@Data
@ToString
public class LoginParam {

    @NotBlank(message = "Username or email must not be blank")
    @Size(max = 255, message = "Length of username or email must not be more than {max}")
    private String username;

    @NotBlank(message = "Password must not be blank")
    @Size(max = 100, message = "Length of password must not be more than {max}")
    private String password;

}
