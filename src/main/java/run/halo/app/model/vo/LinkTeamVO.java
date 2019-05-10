package run.halo.app.model.vo;

import run.halo.app.model.dto.LinkDTO;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Link team vo.
 *
 * @author ryanwang
 * @date : 2019/3/22
 */
@Data
@ToString
public class LinkTeamVO {

    private String team;

    private List<LinkDTO> links;
}
