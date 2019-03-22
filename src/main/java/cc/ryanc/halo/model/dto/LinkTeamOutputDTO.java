package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.dto.base.OutputConverter;
import cc.ryanc.halo.model.entity.Link;
import lombok.Data;

import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2019/3/22
 */
@Data
public class LinkTeamOutputDTO implements OutputConverter<LinkTeamOutputDTO, Link> {

    private String team;

    private List<Link> links;
}
