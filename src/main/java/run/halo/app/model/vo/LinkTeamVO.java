package run.halo.app.model.vo;

import run.halo.app.model.dto.LinkOutputDTO;
import lombok.Data;
import lombok.ToString;
import run.halo.app.model.dto.LinkOutputDTO;

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
