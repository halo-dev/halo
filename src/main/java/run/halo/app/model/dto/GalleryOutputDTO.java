package run.halo.app.model.dto;

import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Gallery;
import lombok.Data;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Gallery;

/**
 * @author : RYAN0UP
 * @date : 2019/3/21
 */
@Data
public class GalleryOutputDTO implements OutputConverter<GalleryOutputDTO, Gallery> {

    private Integer id;

    private String name;

    private String thumbnail;

    private String url;

    private String team;
}
