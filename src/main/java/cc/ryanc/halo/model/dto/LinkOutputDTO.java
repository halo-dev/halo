package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.dto.base.OutputConverter;
import cc.ryanc.halo.model.entity.Link;
import lombok.Data;

/**
 * Link output dto.
 *
 * @author : RYAN0UP
 * @date : 2019/3/21
 */
@Data
public class LinkOutputDTO implements OutputConverter<LinkOutputDTO, Link> {

    private Integer id;

    private String name;

    private String url;

    private String description;

    private String team;
}
