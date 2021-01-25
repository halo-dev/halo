package run.halo.app.model.params;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Photo;

/**
 * Post param.
 *
 * @author ryanwang
 * @date 2019/04/25
 */
@Data
public class PhotoParam implements InputConverter<Photo> {

    @NotBlank(message = "照片名称不能为空")
    private String name;

    private String description;

    private Date takeTime;

    private String location;

    @NotBlank(message = "照片缩略图链接地址不能为空")
    private String thumbnail;

    @NotBlank(message = "照片链接地址不能为空")
    private String url;

    private String team;
}
