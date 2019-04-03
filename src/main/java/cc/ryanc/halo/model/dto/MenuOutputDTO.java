package cc.ryanc.halo.model.dto;

import cc.ryanc.halo.model.dto.base.OutputConverter;
import cc.ryanc.halo.model.entity.Menu;
import lombok.Data;

/**
 * Menu output dto.
 *
 * @author johnniang
 * @date 4/3/19
 */
@Data
public class MenuOutputDTO implements OutputConverter<MenuOutputDTO, Menu> {

    private Integer id;

    private String name;

    private String url;

    private Integer sort;

    private String target;

    private String icon;
}
