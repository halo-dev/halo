package run.halo.app.service.impl;

import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.model.dto.MenuOutputDTO;
import run.halo.app.model.entity.Menu;
import run.halo.app.model.params.MenuParam;
import run.halo.app.repository.MenuRepository;
import run.halo.app.service.MenuService;
import run.halo.app.service.base.AbstractCrudService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.repository.MenuRepository;
import run.halo.app.service.base.AbstractCrudService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MenuService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class MenuServiceImpl extends AbstractCrudService<Menu, Integer> implements MenuService {

    private final MenuRepository menuRepository;

    public MenuServiceImpl(MenuRepository menuRepository) {
        super(menuRepository);
        this.menuRepository = menuRepository;
    }

    @Override
    public List<MenuOutputDTO> listDtos(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        return convertTo(listAll(sort));
    }

    @Override
    public Menu createBy(MenuParam menuParam) {
        Assert.notNull(menuParam, "Menu param must not be null");

        // Check the name
        boolean exists = menuRepository.existsByName(menuParam.getName());

        if (exists) {
            throw new AlreadyExistsException("The menu name " + menuParam.getName() + " has already existed").setErrorData(menuParam.getName());
        }

        // Create an return
        return create(menuParam.convertTo());
    }


    private List<MenuOutputDTO> convertTo(List<Menu> menus) {
        if (CollectionUtils.isEmpty(menus)) {
            return Collections.emptyList();
        }

        return menus.stream()
                .map(menu -> new MenuOutputDTO().<MenuOutputDTO>convertFrom(menu))
                .collect(Collectors.toList());
    }
}
