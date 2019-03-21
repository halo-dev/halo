package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.dto.base.OutputConverter;
import cc.ryanc.halo.model.entity.Tag;
import lombok.Data;

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
