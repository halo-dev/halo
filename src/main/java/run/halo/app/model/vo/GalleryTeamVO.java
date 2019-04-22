package run.halo.app.model.vo;

import lombok.Data;
import lombok.ToString;
import run.halo.app.model.dto.GalleryOutputDTO;
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
public class GalleryTeamVO {

    private String team;

    private List<GalleryOutputDTO> galleries;
}
