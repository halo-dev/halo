package run.halo.app.service.impl;

import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.model.dto.MenuDTO;
import run.halo.app.model.entity.Menu;
import run.halo.app.model.params.MenuParam;
import run.halo.app.model.vo.MenuTeamVO;
import run.halo.app.model.vo.MenuVO;
import run.halo.app.repository.MenuRepository;
import run.halo.app.service.MenuService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.ServiceUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MenuService implementation class.
 *
 * @author ryanwang
 * @date 2019-03-14
 */
@Service
public class MenuServiceImpl extends AbstractCrudService<Menu, Integer> implements MenuService {

    private final MenuRepository menuRepository;

    public MenuServiceImpl(MenuRepository menuRepository) {
        super(menuRepository);
        this.menuRepository = menuRepository;
    }

    @Override
    public List<MenuDTO> listDtos(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        return convertTo(listAll(sort));
    }

    @Override
    public List<MenuTeamVO> listTeamVos(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        // List all menus
        List<MenuDTO> menus = listDtos(sort);

        // Get teams
        Set<String> teams = ServiceUtils.fetchProperty(menus, MenuDTO::getTeam);

        // Convert to team menu list map (Key: team, value: menu list)
        Map<String, List<MenuDTO>> teamMenuListMap = ServiceUtils.convertToListMap(teams, menus, MenuDTO::getTeam);

        List<MenuTeamVO> result = new LinkedList<>();

        // Wrap menu team vo list
        teamMenuListMap.forEach((team, menuList) -> {
            // Build menu team vo
            MenuTeamVO menuTeamVO = new MenuTeamVO();
            menuTeamVO.setTeam(team);
            menuTeamVO.setMenus(menuList);

            // Add it to result
            result.add(menuTeamVO);
        });

        return result;
    }

    @Override
    public List<MenuDTO> listByTeam(String team, Sort sort) {
        List<Menu> menus = menuRepository.findByTeam(team, sort);
        return menus.stream().map(menu -> (MenuDTO) new MenuDTO().convertFrom(menu)).collect(Collectors.toList());
    }

    @Override
    public List<MenuVO> listByTeamAsTree(String team, Sort sort) {
        Assert.notNull(team, "Team must not be null");

        List<Menu> menus = menuRepository.findByTeam(team, sort);

        if (CollectionUtils.isEmpty(menus)) {
            return Collections.emptyList();
        }

        MenuVO topLevelMenu = createTopLevelMenu();

        concreteTree(topLevelMenu, menus);

        List<MenuVO> children = topLevelMenu.getChildren();
        return children;
    }

    @Override
    public Menu createBy(MenuParam menuParam) {
        Assert.notNull(menuParam, "Menu param must not be null");

        // Create an return
        return create(menuParam.convertTo());
    }

    @Override
    public List<MenuVO> listAsTree(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        // List all menu
        List<Menu> menus = listAll(sort);

        if (CollectionUtils.isEmpty(menus)) {
            return Collections.emptyList();
        }

        // Create top menu
        MenuVO topLevelMenu = createTopLevelMenu();

        // Concrete the tree
        concreteTree(topLevelMenu, menus);

        return topLevelMenu.getChildren();
    }

    @Override
    public List<Menu> listByParentId(Integer id) {
        Assert.notNull(id, "Menu parent id must not be null");

        return menuRepository.findByParentId(id);
    }

    @Override
    public List<String> listAllTeams() {
        return menuRepository.findAllTeams();
    }

    @Override
    public Menu create(Menu menu) {
        nameMustNotExist(menu);

        return super.create(menu);
    }

    @Override
    public Menu update(Menu menu) {
        nameMustNotExist(menu);

        return super.update(menu);
    }

    /**
     * Concrete menu tree.
     *
     * @param parentMenu parent menu vo must not be null
     * @param menus      a list of menu
     */
    private void concreteTree(MenuVO parentMenu, List<Menu> menus) {
        Assert.notNull(parentMenu, "Parent menu must not be null");

        if (CollectionUtils.isEmpty(menus)) {
            return;
        }

        // Create children container for removing after
        List<Menu> children = new LinkedList<>();

        menus.forEach(menu -> {
            if (parentMenu.getId().equals(menu.getParentId())) {
                // Save child menu
                children.add(menu);

                // Convert to child menu vo
                MenuVO child = new MenuVO().convertFrom(menu);

                // Init children if absent
                if (parentMenu.getChildren() == null) {
                    parentMenu.setChildren(new LinkedList<>());
                }
                parentMenu.getChildren().add(child);
            }
        });

        // Remove all child menus
        menus.removeAll(children);

        // Foreach children vos
        if (!CollectionUtils.isEmpty(parentMenu.getChildren())) {
            parentMenu.getChildren().forEach(childMenu -> concreteTree(childMenu, menus));
        }
    }

    /**
     * Creates a top level menu.
     *
     * @return top level menu with id 0
     */
    @NonNull
    private MenuVO createTopLevelMenu() {
        MenuVO topMenu = new MenuVO();
        // Set default value
        topMenu.setId(0);
        topMenu.setChildren(new LinkedList<>());
        topMenu.setParentId(-1);
        return topMenu;
    }

    private List<MenuDTO> convertTo(List<Menu> menus) {
        if (CollectionUtils.isEmpty(menus)) {
            return Collections.emptyList();
        }

        return menus.stream()
            .map(menu -> (MenuDTO) new MenuDTO().convertFrom(menu))
            .collect(Collectors.toList());
    }

    private void nameMustNotExist(@NonNull Menu menu) {
        Assert.notNull(menu, "Menu must not be null");

        boolean exist = false;

        if (ServiceUtils.isEmptyId(menu.getId())) {
            // Create action
            exist = menuRepository.existsByName(menu.getName());
        } else {
            // Update action
            exist = menuRepository.existsByIdNotAndName(menu.getId(), menu.getName());
        }

        if (exist) {
            throw new AlreadyExistsException("菜单 " + menu.getName() + " 已存在");
        }
    }
}
