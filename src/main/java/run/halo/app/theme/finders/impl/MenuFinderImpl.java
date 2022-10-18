package run.halo.app.theme.finders.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;
import org.springframework.util.comparator.Comparators;
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
        Collection<MenuItemVo> menuItemVos = populateParentName(listAllMenuItem());
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
                    .sorted(defaultTreeNodeComparator())
                    .toList();
                return menuVo.withMenuItems(menuItems);
            })
            .toList();
    }

    static List<MenuItemVo> listToTree(Collection<MenuItemVo> list) {
        Map<String, List<MenuItemVo>> parentNameIdentityMap = list.stream()
            .filter(menuItemVo -> menuItemVo.getParentName() != null)
            .collect(Collectors.groupingBy(MenuItemVo::getParentName));

        list.forEach(node -> {
            // sort children
            List<MenuItemVo> children =
                parentNameIdentityMap.getOrDefault(node.getMetadata().getName(), List.of())
                    .stream()
                    .sorted(defaultTreeNodeComparator())
                    .toList();
            node.setChildren(children);
        });

        return list.stream()
            .filter(v -> v.getParentName() == null)
            .collect(Collectors.toList());
    }

    List<MenuItemVo> listAllMenuItem() {
        return client.list(MenuItem.class, null, null)
            .map(MenuItemVo::from)
            .collectList()
            .block();
    }

    static Comparator<MenuItemVo> defaultTreeNodeComparator() {
        Function<MenuItemVo, Integer> priority = menuItem -> menuItem.getSpec().getPriority();
        Function<MenuItemVo, Instant> createTime = menuItem -> menuItem.getMetadata()
            .getCreationTimestamp();
        Function<MenuItemVo, String> name = menuItem -> menuItem.getMetadata().getName();

        return Comparator.comparing(priority)
            .thenComparing(createTime, Comparators.nullsLow())
            .thenComparing(name);
    }

    static Collection<MenuItemVo> populateParentName(List<MenuItemVo> menuItemVos) {
        Map<String, MenuItemVo> nameIdentityMap = menuItemVos.stream()
            .collect(Collectors.toMap(menuItem -> menuItem.getMetadata().getName(),
                Function.identity()));

        nameIdentityMap.forEach((name, value) -> {
            LinkedHashSet<String> children = value.getSpec().getChildren();
            if (children != null) {
                for (String child : children) {
                    MenuItemVo childNode = nameIdentityMap.get(child);
                    childNode.setParentName(name);
                }
            }
        });
        return nameIdentityMap.values();
    }
}
