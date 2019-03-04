package cc.ryanc.halo.model.params;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Parameter of password resetting.
 *
 * @author johnniang
 */
@Data
public class PasswordResetParam {

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, max = 18, message = "Length of password must be between {min} to {max}")
    private String password;

    @NotBlank(message = "Confirm password must not be blank")
    @Size(min = 6, max = 18, message = "Length of confirm password must be between {min} to {max}")
    private String definePassword;

    @NotBlank(message = "Reset code must not be blank")
    private String code;
}
