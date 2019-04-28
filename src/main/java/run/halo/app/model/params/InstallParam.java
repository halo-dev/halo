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
    private String url;

}
