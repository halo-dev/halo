package cc.ryanc.halo.service;

import cc.ryanc.halo.model.dto.MenuOutputDTO;
import cc.ryanc.halo.model.entity.Menu;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * Menu service.
 *
 * @author johnniang
 */
public interface MenuService extends CrudService<Menu, Integer> {

    /**
     * Lists all menu dtos.
     *
     * @param sort must not be null
     * @return a list of menu output dto
     */
    @NonNull
    List<MenuOutputDTO> listDtos(@NonNull Sort sort);
}
