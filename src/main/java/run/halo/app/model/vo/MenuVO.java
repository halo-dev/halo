package run.halo.app.model.vo;

import lombok.Data;
import run.halo.app.model.dto.MenuOutputDTO;

import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2019-04-07
 */
@Data
public class MenuVO extends MenuOutputDTO {

    private List<MenuVO> children;
}
