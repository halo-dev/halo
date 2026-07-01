package run.halo.app.theme.finders.impl;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.util.comparator.Comparators;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.NotFoundException;
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
@AllArgsConstructor
public class MenuFinderImpl implements MenuFinder {

    private final ReactiveExtensionClient client;
    private final SystemConfigFetcher environmentFetcher;

    @Override
    public Mono<MenuVo> getByName(String name) {
        return client.fetch(Menu.class, name)
                .flatMap(this::withMenuItems)
                .switchIfEmpty(Mono.error(() -> new NotFoundException("Menu with name " + name + " not found")));
    }

    @Override
    public Mono<MenuVo> getPrimary() {
        return listAllMenus()
                .collectList()
                .flatMap(menus -> {
                    if (CollectionUtils.isEmpty(menus)) {
                        return Mono.empty();
                    }
                    return environmentFetcher
                            .fetch(SystemSetting.Menu.GROUP, SystemSetting.Menu.class)
                            .map(SystemSetting.Menu::getPrimary)
                            .map(primaryConfig -> menus.stream()
                                    .filter(menu -> menu.getMetadata().getName().equals(primaryConfig))
                                    .findAny()
                                    .orElse(menus.get(0)))
                            .defaultIfEmpty(menus.get(0));
                })
                .flatMap(this::withMenuItems)
                .switchIfEmpty(Mono.error(() -> new NotFoundException("No primary menu found")));
    }

    Flux<Menu> listAllMenus() {
        return client.listAll(Menu.class, ListOptions.builder().build(), Sort.unsorted());
    }

    Flux<MenuVo> listAll() {
        return listAllMenus().map(MenuVo::from);
    }

    Flux<MenuVo> listAsTree() {
        return listAllMenus().flatMap(this::withMenuItems);
    }

    Mono<MenuVo> withMenuItems(Menu menu) {
        return listMenuItemsByMenuName(menu.getMetadata().getName())
                .collectList()
                .map(MenuFinderImpl::listToTree)
                .map(menuItems -> MenuVo.from(menu).withMenuItems(menuItems));
    }

    static List<MenuItemVo> listToTree(Collection<MenuItemVo> list) {
        Map<String, MenuItemVo> nameIdentityMap = list.stream()
                .collect(Collectors.toMap(menuItem -> menuItem.getMetadata().getName(), v -> v));
        Map<String, List<MenuItemVo>> parentNameIdentityMap = list.stream()
                .filter(menuItemVo -> hasValidParent(menuItemVo, nameIdentityMap))
                .collect(Collectors.groupingBy(MenuItemVo::getParentName));

        list.forEach(node -> {
            // sort children
            List<MenuItemVo> children =
                    parentNameIdentityMap.getOrDefault(node.getMetadata().getName(), List.of()).stream()
                            .sorted(defaultTreeNodeComparator())
                            .toList();
            node.setChildren(children);
        });

        return list.stream()
                .filter(v -> !hasValidParent(v, nameIdentityMap))
                .sorted(defaultTreeNodeComparator())
                .toList();
    }

    private static boolean hasValidParent(MenuItemVo menuItem, Map<String, MenuItemVo> nameIdentityMap) {
        var parentName = menuItem.getParentName();
        if (parentName == null
                || Objects.equals(parentName, menuItem.getMetadata().getName())) {
            return false;
        }
        if (!nameIdentityMap.containsKey(parentName)) {
            return false;
        }
        var ancestorName = parentName;
        while (ancestorName != null) {
            if (Objects.equals(ancestorName, menuItem.getMetadata().getName())) {
                return false;
            }
            var ancestor = nameIdentityMap.get(ancestorName);
            if (ancestor == null) {
                return true;
            }
            ancestorName = ancestor.getParentName();
        }
        return true;
    }

    Flux<MenuItemVo> listMenuItemsByMenuName(String menuName) {
        var listOptions = ListOptions.builder()
                .andQuery(Queries.equal("spec.menuName", menuName))
                .build();
        return client.listAll(MenuItem.class, listOptions, Sort.unsorted()).map(MenuItemVo::from);
    }

    static Comparator<MenuItemVo> defaultTreeNodeComparator() {
        java.util.function.Function<MenuItemVo, Integer> priority = menuItem -> Optional.ofNullable(menuItem.getSpec())
                .map(MenuItem.MenuItemSpec::getPriority)
                .orElse(0);
        java.util.function.Function<MenuItemVo, Instant> createTime =
                menuItem -> menuItem.getMetadata().getCreationTimestamp();
        java.util.function.Function<MenuItemVo, String> name =
                menuItem -> menuItem.getMetadata().getName();

        return Comparator.comparing(priority)
                .thenComparing(createTime, Comparators.nullsLow())
                .thenComparing(name);
    }
}
