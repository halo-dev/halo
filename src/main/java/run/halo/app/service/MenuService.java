package run.halo.app.service;

import run.halo.app.model.dto.MenuOutputDTO;
import run.halo.app.model.entity.Menu;
import run.halo.app.model.params.MenuParam;
import run.halo.app.service.base.CrudService;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import run.halo.app.service.base.CrudService;

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

    /**
     * Creates a menu.
     *
     * @param menuParam must not be null
     * @return created menu
     */
    @NonNull
    Menu createBy(@NonNull MenuParam menuParam);
}
