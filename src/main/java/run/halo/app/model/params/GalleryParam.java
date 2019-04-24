package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Gallery;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * Post param.
 *
 * @author RYAN0UP
 * @date 2019/04/25
 */
@Data
public class GalleryParam implements InputConverter<Gallery> {

    @NotBlank(message = "Gallery name must not be blank")
    private String name;

    private String description;

    private Date takeTime;

    private String location;

    @NotBlank(message = "Gallery thumbnail must not be blank")
    private String thumbnail;

    @NotBlank(message = "Gallery url must not be blank")
    private String url;

    private String team;
}
