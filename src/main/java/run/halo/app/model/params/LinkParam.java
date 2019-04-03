package run.halo.app.model.params;

import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Link;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Link param.
 *
 * @author johnniang
 * @date 4/3/19
 */
@Data
public class LinkParam implements InputConverter<Link> {

    @NotBlank(message = "Link name must not be blank")
    @Size(max = 255, message = "Length of link name must not be more than {max}")
    private String name;

    @NotBlank(message = "Link url must not be blank")
    @Size(max = 1023, message = "Length of link url must not be more than {max}")
    @URL(message = "Link url format is incorrect")
    private String url;

    @Size(max = 1023, message = "Length of link logo must not be more than {max}")
    private String logo;

    @Size(max = 255, message = "Length of link description must not be more than {max}")
    private String description;

    @Size(max = 255, message = "Length of link team must not be more than {max}")
    private String team;

}
