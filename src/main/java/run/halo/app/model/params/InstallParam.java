package run.halo.app.model.params;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Install parameters.
 *
 * @author johnniang
 * @date 3/19/19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InstallParam extends UserParam {

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
     * Password.
     */
    @NotBlank(message = "Password must not be blank")
    @Size(max = 100, message = "Length of password must not be more than {max}")
    private String password;
}
