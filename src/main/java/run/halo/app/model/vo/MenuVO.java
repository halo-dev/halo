package run.halo.app.model.vo;

import lombok.Data;
import run.halo.app.model.dto.MenuDTO;

import java.util.List;

/**
 * @author ryanwang
 * @date : 2019-04-07
 */
@Data
public class MenuVO extends MenuDTO {

    private List<MenuVO> children;
}
