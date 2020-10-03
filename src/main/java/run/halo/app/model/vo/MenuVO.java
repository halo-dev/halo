package run.halo.app.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.dto.MenuDTO;

import java.util.List;

/**
 * @author ryanwang
 * @date 2019-04-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MenuVO extends MenuDTO {

    private List<MenuVO> children;
}
