package run.halo.app.model.params;

import lombok.Data;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Photo;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * Post param.
 *
 * @author ryanwang
 * @date 2019/04/25
 */
@Data
public class PhotoParam implements InputConverter<Photo> {

    @NotBlank(message = "Photo name must not be blank")
    private String name;

    private String description;

    private Date takeTime;

    private String location;

    @NotBlank(message = "Photo thumbnail must not be blank")
    private String thumbnail;

    @NotBlank(message = "Photo url must not be blank")
    private String url;

    private String team;
}
