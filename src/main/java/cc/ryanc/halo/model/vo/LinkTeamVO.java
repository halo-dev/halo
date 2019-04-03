package cc.ryanc.halo.model.vo;

import cc.ryanc.halo.model.dto.LinkOutputDTO;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Link team vo.
 *
 * @author : RYAN0UP
 * @date : 2019/3/22
 */
@Data
@ToString
public class LinkTeamVO {

    private String team;

    private List<LinkOutputDTO> links;
}
