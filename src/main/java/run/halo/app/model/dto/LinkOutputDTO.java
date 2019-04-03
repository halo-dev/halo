package run.halo.app.model.dto;

import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Link;
import lombok.Data;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Link;

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
