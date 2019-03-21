package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.dto.base.OutputConverter;
import cc.ryanc.halo.model.entity.Gallery;
import lombok.Data;

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
}
