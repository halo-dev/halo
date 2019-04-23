package run.halo.app.model.dto;

import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Tag;
import lombok.Data;

/**
 * Tag output dto.
 *
 * @author johnniang
 * @date 3/19/19
 */
@Data
public class TagDTO implements OutputConverter<TagDTO, Tag> {

    private Integer id;

    private String name;

    private String slugName;
}
