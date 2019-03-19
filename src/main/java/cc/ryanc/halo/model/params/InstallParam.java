package cc.ryanc.halo.model.params;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * Install parameters.
 *
 * @author johnniang
 * @date 3/19/19
 */
@Data
public class InstallParam {

    /**
     * Blog locale.
     */
    @NotBlank(message = "Blog locale must not be blank")
    private String locale;

    /**
     * Blog title.
     */
    @NotBlank(message = "Blog title must not be blank")
    private String title;

    /**
     * Blog url.
     */
    @NotBlank(message = "Blog url must not be blank")
    private String url;

    /**
     * Username.
     */
    @NotBlank(message = "Username must not be blank")
    private String username;

    /**
     * Nickname.
     */
    @NotBlank(message = "Nickname must not be blank")
    private String nickname;

    /**
     * Email.
     */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "It is not an email format")
    private String email;

    /**
     * Password.
     */
    @NotBlank(message = "Password must not be blank")
    private String password;
}
