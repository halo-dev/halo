package run.halo.app.model.dto;

import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Menu;
import lombok.Data;

/**
 * Menu output dto.
 *
 * @author johnniang
 * @date 4/3/19
 */
@Data
public class MenuDTO implements OutputConverter<MenuDTO, Menu> {

    private Integer id;

    private String name;

    private String url;

    private Integer priority;

    private String target;

    private String icon;

    private Integer parentId;
}
