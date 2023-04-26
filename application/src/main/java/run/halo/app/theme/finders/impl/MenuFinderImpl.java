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
import lombok.AllArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.comparator.Comparators;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
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
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    @Override
    public Mono<MenuVo> getByName(String name) {
        return listAsTree()
            .filter(menu -> menu.getMetadata().getName().equals(name))
            .next()
            .switchIfEmpty(Mono.error(
                () -> new NotFoundException("Menu with name " + name + " not found")));
    }

    @Override
    public Mono<MenuVo> getPrimary() {
        return listAsTree().collectList()
            .flatMap(menuVos -> {
                if (CollectionUtils.isEmpty(menuVos)) {
                    return Mono.empty();
                }
                return environmentFetcher.fetch(SystemSetting.Menu.GROUP, SystemSetting.Menu.class)
                    .map(SystemSetting.Menu::getPrimary)
                    .map(primaryConfig -> menuVos.stream()
                        .filter(menuVo -> menuVo.getMetadata().getName().equals(primaryConfig))
                        .findAny()
                        .orElse(menuVos.get(0))
                    )
                    .defaultIfEmpty(menuVos.get(0));
            })
            .switchIfEmpty(
                Mono.error(() -> new NotFoundException("No primary menu found"))
            );
    }

    Flux<MenuVo> listAll() {
        return client.list(Menu.class, null, null)
            .map(MenuVo::from);
    }

    Flux<MenuVo> listAsTree() {
        return listAllMenuItem()
            .collectList()
            .map(MenuFinderImpl::populateParentName)
            .flatMapMany(menuItemVos -> {
                List<MenuItemVo> treeList = listToTree(menuItemVos);
                Map<String, MenuItemVo> nameItemRootNodeMap = treeList.stream()
                    .collect(Collectors.toMap(item -> item.getMetadata().getName(),
                        Function.identity()));
                return listAll()
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
                    });
            });
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

    Flux<MenuItemVo> listAllMenuItem() {
        return client.list(MenuItem.class, null, null)
            .map(MenuItemVo::from);
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
            if (children == null) {
                return;
            }
            for (String child : children) {
                MenuItemVo childNode = nameIdentityMap.get(child);
                if (childNode != null) {
                    childNode.setParentName(name);
                }
            }
        });
        return nameIdentityMap.values();
    }
}
