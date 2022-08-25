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
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.MenuFinder;
import run.halo.app.theme.finders.SubscriberUtils;
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
    public List<MenuVo> listAll() {
        Mono<List<MenuVo>> listMono = client.list(Menu.class, null, null)
            .map(MenuVo::from)
            .collectList();
        return SubscriberUtils.subscribe(listMono);
    }

    @Override
    public List<MenuVo> listAsTree() {
        List<MenuItemVo> menuItemVos = populateParentName(listAllMenuItem());
        List<MenuItemVo> treeList = listToTree(menuItemVos);
        Map<String, MenuItemVo> nameItemRootNodeMap = treeList.stream()
            .collect(Collectors.toMap(MenuItemVo::getName, Function.identity()));
        return listAll().stream()
            .map(menuVo -> {
                List<MenuItemVo> menuItems = menuVo.getMenuItemNames().stream()
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
        list.forEach(node -> node.setChildren(nameIdentityMap.get(node.getName())));
        // clear map to release memory
        nameIdentityMap.clear();
        return list.stream()
            .filter(v -> v.getParentName() == null)
            .collect(Collectors.toList());
    }

    @Override
    public List<MenuItemVo> listAllMenuItem() {
        Function<MenuItem, Integer> priority = menuItem -> menuItem.getSpec().getPriority();
        Function<MenuItem, String> name = menuItem -> menuItem.getMetadata().getName();

        Mono<List<MenuItemVo>> listMono = client.list(MenuItem.class, null,
                Comparator.comparing(priority).thenComparing(name).reversed()
            )
            .map(MenuItemVo::from)
            .collectList();
        return SubscriberUtils.subscribe(listMono);
    }

    static List<MenuItemVo> populateParentName(List<MenuItemVo> menuItemVos) {
        Map<String, MenuItemVo> nameIdentityMap = menuItemVos.stream()
            .collect(Collectors.toMap(MenuItemVo::getName, Function.identity()));

        Map<String, MenuItemVo> treeVoMap = new HashMap<>();
        // populate parentName
        menuItemVos.forEach(menuItemVo -> {
            final String parentName = menuItemVo.getName();
            treeVoMap.putIfAbsent(parentName, menuItemVo);
            LinkedHashSet<String> children = menuItemVo.getChildrenNames();
            if (CollectionUtils.isEmpty(children)) {
                return;
            }
            children.forEach(childrenName -> {
                MenuItemVo childrenVo = nameIdentityMap.get(childrenName);
                childrenVo.setParentName(parentName);
                treeVoMap.putIfAbsent(childrenVo.getName(), childrenVo);
            });
        });
        // clear map to release memory
        nameIdentityMap.clear();
        return treeVoMap.values()
            .stream()
            .sorted(Comparator.comparing(MenuItemVo::getPriority))
            .toList();
    }
}
