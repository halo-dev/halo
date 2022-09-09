package run.halo.app.theme.finders.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.MenuFinder;
import run.halo.app.theme.finders.vo.MenuItemVo;
import run.halo.app.theme.finders.vo.MenuVo;

/**
 * A default implementation for {@link MenuFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("menuFinder")
public class MenuFinderImpl implements MenuFinder {

    private final ReactiveExtensionClient client;

    public MenuFinderImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public MenuVo getByName(String name) {
        return listAsTree().stream()
            .filter(menu -> menu.getMetadata().getName().equals(name))
            .findAny()
            .orElse(null);
    }

    @Override
    public MenuVo getDefault() {
        List<MenuVo> menuVos = listAsTree();
        if (CollectionUtils.isEmpty(menuVos)) {
            return null;
        }
        // TODO If there are multiple groups of menus,
        //  return the first as the default, and consider optimizing it later
        return menuVos.get(0);
    }


    List<MenuVo> listAll() {
        return client.list(Menu.class, null, null)
            .map(MenuVo::from)
            .collectList()
            .block();
    }

    List<MenuVo> listAsTree() {
        List<MenuItemVo> menuItemVos = populateParentName(listAllMenuItem());
        List<MenuItemVo> treeList = listToTree(menuItemVos);
        Map<String, MenuItemVo> nameItemRootNodeMap = treeList.stream()
            .collect(Collectors.toMap(item -> item.getMetadata().getName(), Function.identity()));

        return listAll().stream()
            .map(menuVo -> {
                LinkedHashSet<String> menuItemNames = menuVo.getSpec().getMenuItems();
                if (menuItemNames == null) {
                    return menuVo.withMenuItems(List.of());
                }
                List<MenuItemVo> menuItems = menuItemNames.stream()
                    .map(nameItemRootNodeMap::get)
                    .filter(Objects::nonNull)
                    .toList();
                return menuVo.withMenuItems(menuItems);
            })
            .toList();
    }

    static List<MenuItemVo> listToTree(List<MenuItemVo> list) {
        Map<String, List<MenuItemVo>> nameIdentityMap = list.stream()
            .filter(item -> item.getParentName() != null)
            .collect(Collectors.groupingBy(MenuItemVo::getParentName));
        list.forEach(node -> node.setChildren(nameIdentityMap.get(node.getMetadata().getName())));
        // clear map to release memory
        nameIdentityMap.clear();
        return list.stream()
            .filter(v -> v.getParentName() == null)
            .collect(Collectors.toList());
    }

    List<MenuItemVo> listAllMenuItem() {
        Function<MenuItem, Integer> priority = menuItem -> menuItem.getSpec().getPriority();
        Function<MenuItem, String> name = menuItem -> menuItem.getMetadata().getName();

        return client.list(MenuItem.class, null,
                Comparator.comparing(priority).thenComparing(name).reversed()
            )
            .map(MenuItemVo::from)
            .collectList()
            .block();
    }

    static List<MenuItemVo> populateParentName(List<MenuItemVo> menuItemVos) {
        Map<String, MenuItemVo> nameIdentityMap = menuItemVos.stream()
            .collect(Collectors.toMap(menuItem -> menuItem.getMetadata().getName(),
                Function.identity()));

        Map<String, MenuItemVo> treeVoMap = new HashMap<>();
        // populate parentName
        menuItemVos.forEach(menuItemVo -> {
            final String parentName = menuItemVo.getMetadata().getName();
            treeVoMap.putIfAbsent(parentName, menuItemVo);
            LinkedHashSet<String> children = menuItemVo.getSpec().getChildren();
            if (CollectionUtils.isEmpty(children)) {
                return;
            }
            children.forEach(childrenName -> {
                MenuItemVo childrenVo = nameIdentityMap.get(childrenName);
                childrenVo.setParentName(parentName);
                treeVoMap.putIfAbsent(childrenVo.getMetadata().getName(), childrenVo);
            });
        });
        // clear map to release memory
        nameIdentityMap.clear();
        Function<MenuItemVo, Integer> priorityCmp = menuItem -> menuItem.getSpec().getPriority();
        return treeVoMap.values()
            .stream()
            .sorted(Comparator.comparing(priorityCmp))
            .toList();
    }
}
