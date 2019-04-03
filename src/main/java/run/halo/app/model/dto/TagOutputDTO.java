package run.halo.app.model.dto;

import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Tag;
import lombok.Data;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Tag;

/**
 * Tag output dto.
 *
 * @author johnniang
 * @date 3/19/19
 */
@Data
public class TagOutputDTO implements OutputConverter<TagOutputDTO, Tag> {

    private Integer id;

    private String name;

    private String slugName;
}
