package run.halo.app.model.vo;

import java.util.List;
import lombok.Data;
import lombok.ToString;
import run.halo.app.model.dto.LinkDTO;

/**
 * Link team vo.
 *
 * @author ryanwang
 * @date 2019/3/22
 */
@Data
@ToString
public class LinkTeamVO {

    private String team;

    private List<LinkDTO> links;
}
