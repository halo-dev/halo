package run.halo.app.core.endpoint.console;

import static run.halo.app.extension.index.query.Queries.equal;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.NotFoundException;

@Component
public class MenuItemConsoleService {

    private final ReactiveExtensionClient client;

    public MenuItemConsoleService(ReactiveExtensionClient client) {
        this.client = client;
    }

    Mono<List<MenuItemTreeNode>> listTree(String menuName) {
        return listMenuItems(menuName).collectList().map(MenuItemConsoleService::listToTree);
    }

    Mono<List<MenuItemTreeNode>> updatePosition(String name, MenuItemPositionRequest request) {
        var menuName = normalizeRequired(request.menuName(), "menuName is required.");
        return Mono.defer(() -> move(name, menuName, normalize(request.parentName()), normalize(request.beforeName())))
                .retryWhen(Retry.backoff(1, Duration.ofMillis(100))
                        .filter(OptimisticLockingFailureException.class::isInstance))
                .onErrorMap(
                        Exceptions::isRetryExhausted,
                        error -> new ResponseStatusException(
                                HttpStatus.CONFLICT, "Menu item position update conflicted.", error));
    }

    private Mono<List<MenuItemTreeNode>> move(
            String name, String menuName, String targetParentName, String beforeName) {
        return client.fetch(MenuItem.class, name)
                .switchIfEmpty(Mono.error(() -> new NotFoundException("MenuItem with name " + name + " not found")))
                .flatMap(moved -> {
                    if (!Objects.equals(menuName, menuNameOf(moved))) {
                        return Mono.error(new ServerWebInputException("MenuItem does not belong to menu " + menuName));
                    }
                    return listMenuItems(menuName)
                            .collectList()
                            .flatMap(items -> applyMove(name, menuName, targetParentName, beforeName, items));
                });
    }

    private Mono<List<MenuItemTreeNode>> applyMove(
            String name, String menuName, String targetParentName, String beforeName, List<MenuItem> items) {
        var itemMap = items.stream()
                .collect(Collectors.toMap(
                        item -> item.getMetadata().getName(),
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new));
        var moved = itemMap.get(name);
        if (moved == null) {
            return Mono.error(new ServerWebInputException("MenuItem does not belong to menu " + menuName));
        }

        if (targetParentName != null) {
            if (Objects.equals(targetParentName, name)) {
                return Mono.error(new ServerWebInputException("Cannot move a MenuItem under itself."));
            }
            if (!itemMap.containsKey(targetParentName)) {
                return Mono.error(new ServerWebInputException("Parent MenuItem was not found in the selected menu."));
            }
            if (isDescendant(targetParentName, name, itemMap)) {
                return Mono.error(new ServerWebInputException("Cannot move a MenuItem under one of its descendants."));
            }
        }

        if (beforeName != null && !itemMap.containsKey(beforeName)) {
            return Mono.error(new ServerWebInputException("Before MenuItem was not found in the selected menu."));
        }

        var originalStates = items.stream()
                .collect(Collectors.toMap(
                        item -> item.getMetadata().getName(),
                        item -> new HierarchyState(parentNameOf(item), priorityOf(item)),
                        (left, right) -> left,
                        LinkedHashMap::new));
        var originalParentName = parentNameOf(moved);

        var targetSiblings = siblings(items, targetParentName, name);
        int insertIndex = targetSiblings.size();
        if (beforeName != null) {
            insertIndex = indexOf(targetSiblings, beforeName);
            if (insertIndex < 0) {
                return Mono.error(new ServerWebInputException("Before MenuItem is not a target sibling."));
            }
        }
        targetSiblings.add(insertIndex, moved);
        assignPriorities(targetSiblings, targetParentName);

        if (!Objects.equals(originalParentName, targetParentName)) {
            assignPriorities(siblings(items, originalParentName, name), originalParentName);
        }

        var changedItems = items.stream()
                .filter(item -> hasHierarchyChanged(
                        item, originalStates.get(item.getMetadata().getName())))
                .toList();

        return Flux.fromIterable(changedItems)
                .concatMap(client::update)
                .then(Mono.fromSupplier(() -> listToTree(items)));
    }

    private Flux<MenuItem> listMenuItems(String menuName) {
        var listOptions =
                ListOptions.builder().andQuery(equal("spec.menuName", menuName)).build();
        return client.listAll(MenuItem.class, listOptions, Sort.unsorted());
    }

    static List<MenuItemTreeNode> listToTree(Collection<MenuItem> items) {
        Map<String, MenuItemTreeNode> itemMap = items.stream()
                .map(item -> new MenuItemTreeNode(item, new ArrayList<>()))
                .collect(Collectors.toMap(
                        node -> node.getMenuItem().getMetadata().getName(),
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new));
        Map<String, String> parentMap = validParentMap(itemMap);
        Set<String> cyclicChainNames = cyclicChainNames(parentMap);

        itemMap.forEach((name, node) -> {
            var parentName = parentMap.get(name);
            if (parentName != null && !cyclicChainNames.contains(name)) {
                itemMap.get(parentName).getChildren().add(node);
            }
        });

        var roots = itemMap.values().stream()
                .filter(node -> {
                    var name = node.getMenuItem().getMetadata().getName();
                    return !parentMap.containsKey(name) || cyclicChainNames.contains(name);
                })
                .collect(Collectors.toCollection(ArrayList::new));
        sortTree(roots);
        return roots;
    }

    private static Map<String, String> validParentMap(Map<String, MenuItemTreeNode> itemMap) {
        Map<String, String> parentMap = new LinkedHashMap<>();
        itemMap.forEach((name, node) -> {
            var parentName = parentNameOf(node.getMenuItem());
            if (parentName != null && !Objects.equals(parentName, name) && itemMap.containsKey(parentName)) {
                parentMap.put(name, parentName);
            }
        });
        return parentMap;
    }

    private static Set<String> cyclicChainNames(Map<String, String> parentMap) {
        Set<String> cyclicNames = new HashSet<>();
        for (String name : parentMap.keySet()) {
            var path = new ArrayList<String>();
            var visiting = new HashSet<String>();
            var current = name;
            while (current != null && parentMap.containsKey(current)) {
                if (!visiting.add(current)) {
                    cyclicNames.addAll(path);
                    break;
                }
                path.add(current);
                current = parentMap.get(current);
            }
        }
        return cyclicNames;
    }

    private static void sortTree(List<MenuItemTreeNode> nodes) {
        nodes.sort(Comparator.comparing(MenuItemTreeNode::getMenuItem, defaultMenuItemComparator()));
        nodes.forEach(node -> sortTree(node.getChildren()));
    }

    private static Comparator<MenuItem> defaultMenuItemComparator() {
        return Comparator.comparing(MenuItemConsoleService::priorityOf)
                .thenComparing(
                        MenuItemConsoleService::creationTimestampOf, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(item -> item.getMetadata().getName());
    }

    private static Instant creationTimestampOf(MenuItem menuItem) {
        return Optional.ofNullable(menuItem.getMetadata())
                .map(MetadataOperator::getCreationTimestamp)
                .orElse(null);
    }

    private static int priorityOf(MenuItem menuItem) {
        return Optional.ofNullable(menuItem.getSpec())
                .map(MenuItem.MenuItemSpec::getPriority)
                .orElse(0);
    }

    private static String parentNameOf(MenuItem menuItem) {
        return normalize(Optional.ofNullable(menuItem.getSpec())
                .map(MenuItem.MenuItemSpec::getParent)
                .orElse(null));
    }

    private static String menuNameOf(MenuItem menuItem) {
        return Optional.ofNullable(menuItem.getSpec())
                .map(MenuItem.MenuItemSpec::getMenuName)
                .orElse(null);
    }

    private static boolean isDescendant(String candidateName, String ancestorName, Map<String, MenuItem> itemMap) {
        var current = candidateName;
        var visited = new HashSet<String>();
        while (current != null) {
            if (Objects.equals(current, ancestorName)) {
                return true;
            }
            if (!visited.add(current)) {
                throw new ServerWebInputException("Target parent has a cyclic parent chain.");
            }
            current = Optional.ofNullable(itemMap.get(current))
                    .map(MenuItemConsoleService::parentNameOf)
                    .orElse(null);
        }
        return false;
    }

    private static List<MenuItem> siblings(List<MenuItem> items, String parentName, String excludingName) {
        return items.stream()
                .filter(item -> !Objects.equals(item.getMetadata().getName(), excludingName))
                .filter(item -> Objects.equals(parentNameOf(item), parentName))
                .sorted(defaultMenuItemComparator())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static int indexOf(List<MenuItem> menuItems, String name) {
        for (int i = 0; i < menuItems.size(); i++) {
            if (Objects.equals(menuItems.get(i).getMetadata().getName(), name)) {
                return i;
            }
        }
        return -1;
    }

    private static void assignPriorities(List<MenuItem> siblings, String parentName) {
        for (int i = 0; i < siblings.size(); i++) {
            var item = siblings.get(i);
            if (item.getSpec() == null) {
                item.setSpec(new MenuItem.MenuItemSpec());
            }
            item.getSpec().setParent(parentName);
            item.getSpec().setPriority(i);
        }
    }

    private static boolean hasHierarchyChanged(MenuItem item, HierarchyState original) {
        if (original == null) {
            return false;
        }
        return !Objects.equals(parentNameOf(item), original.parentName()) || priorityOf(item) != original.priority();
    }

    private static String normalizeRequired(String value, String message) {
        var normalized = normalize(value);
        if (normalized == null) {
            throw new ServerWebInputException(message);
        }
        return normalized;
    }

    private static String normalize(String value) {
        return org.springframework.util.StringUtils.hasText(value) ? value : null;
    }

    private record HierarchyState(String parentName, int priority) {}
}
