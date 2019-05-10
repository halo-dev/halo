package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.User;
import run.halo.app.model.support.AllCheck;
import run.halo.app.model.support.CreateCheck;
import run.halo.app.model.support.UpdateCheck;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

/**
 * User param.
 *
 * @author johnniang
 * @date 3/19/19
 */
@Data
public class UserParam implements InputConverter<User> {

    @NotBlank(message = "Username must not be blank", groups = {AllCheck.class})
    @Size(max = 50, message = "Length of username must not be more than {max}", groups = {AllCheck.class})
    private String username;

    @NotBlank(message = "Nickname must not be blank", groups = {AllCheck.class})
    @Size(max = 255, message = "Length of nickname must not be more than {max}", groups = {AllCheck.class})
    private String nickname;

    @Email(message = "The email format is incorrect", groups = {AllCheck.class})
    @NotBlank(message = "Email must not be blank", groups = {AllCheck.class})
    @Size(max = 127, message = "Length of email must not be more than {max}", groups = {AllCheck.class})
    private String email;

    @Null(groups = UpdateCheck.class)
    @Size(min = 8, max = 100, message = "Length of password must be between {min} and {max}", groups = {CreateCheck.class})
    private String password;

    @Size(max = 1023, message = "Length of avatar link must not be more than {max}", groups = {AllCheck.class})
    private String avatar;

    @Size(max = 1023, message = "Length of description must not be more than {max}", groups = {AllCheck.class})
    private String description;

}
