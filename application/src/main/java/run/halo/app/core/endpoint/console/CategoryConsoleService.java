package run.halo.app.core.endpoint.console;

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
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.NotFoundException;

@Component
public class CategoryConsoleService {

    private final ReactiveExtensionClient client;

    public CategoryConsoleService(ReactiveExtensionClient client) {
        this.client = client;
    }

    Mono<List<CategoryTreeNode>> listTree() {
        return listCategories().collectList().map(CategoryConsoleService::listToTree);
    }

    Mono<List<CategoryTreeNode>> updatePosition(String name, CategoryPositionRequest request) {
        return Mono.defer(() -> move(name, normalize(request.parentName()), normalize(request.beforeName())))
                .retryWhen(Retry.backoff(1, Duration.ofMillis(100))
                        .filter(OptimisticLockingFailureException.class::isInstance))
                .onErrorMap(
                        Exceptions::isRetryExhausted,
                        error -> new ResponseStatusException(
                                HttpStatus.CONFLICT, "Category position update conflicted.", error));
    }

    private Mono<List<CategoryTreeNode>> move(String name, String targetParentName, String beforeName) {
        return listCategories()
                .collectList()
                .flatMap(categories -> applyMove(name, targetParentName, beforeName, categories));
    }

    private Mono<List<CategoryTreeNode>> applyMove(
            String name, String targetParentName, String beforeName, List<Category> categories) {
        var categoryMap = categories.stream()
                .collect(Collectors.toMap(
                        category -> category.getMetadata().getName(),
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new));
        var moved = categoryMap.get(name);
        if (moved == null) {
            return Mono.error(new NotFoundException("Category with name " + name + " not found"));
        }

        if (targetParentName != null) {
            if (Objects.equals(targetParentName, name)) {
                return Mono.error(new ServerWebInputException("Cannot move a Category under itself."));
            }
            if (!categoryMap.containsKey(targetParentName)) {
                return Mono.error(new ServerWebInputException("Parent Category was not found."));
            }
            if (isDescendant(targetParentName, name, categoryMap)) {
                return Mono.error(new ServerWebInputException("Cannot move a Category under one of its descendants."));
            }
        }

        if (beforeName != null && !categoryMap.containsKey(beforeName)) {
            return Mono.error(new ServerWebInputException("Before Category was not found."));
        }

        var originalStates = categories.stream()
                .collect(Collectors.toMap(
                        category -> category.getMetadata().getName(),
                        category -> new HierarchyState(parentNameOf(category), priorityOf(category)),
                        (left, right) -> left,
                        LinkedHashMap::new));
        var originalParentName = parentNameOf(moved);

        var targetSiblings = siblings(categories, targetParentName, name);
        int insertIndex = targetSiblings.size();
        if (beforeName != null) {
            insertIndex = indexOf(targetSiblings, beforeName);
            if (insertIndex < 0) {
                return Mono.error(new ServerWebInputException("Before Category is not a target sibling."));
            }
        }
        targetSiblings.add(insertIndex, moved);
        assignPriorities(targetSiblings, targetParentName);

        if (!Objects.equals(originalParentName, targetParentName)) {
            assignPriorities(siblings(categories, originalParentName, name), originalParentName);
        }

        var changedCategories = categories.stream()
                .filter(category -> hasHierarchyChanged(
                        category, originalStates.get(category.getMetadata().getName())))
                .toList();

        return Flux.fromIterable(changedCategories)
                .concatMap(client::update)
                .then(Mono.fromSupplier(() -> listToTree(categories)));
    }

    private Flux<Category> listCategories() {
        return client.listAll(Category.class, new ListOptions(), Sort.unsorted());
    }

    static List<CategoryTreeNode> listToTree(Collection<Category> categories) {
        Map<String, CategoryTreeNode> categoryMap = categories.stream()
                .map(category -> new CategoryTreeNode(category, new ArrayList<>()))
                .collect(Collectors.toMap(
                        node -> node.getCategory().getMetadata().getName(),
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new));
        Map<String, String> parentMap = validParentMap(categoryMap);
        Set<String> cyclicNames = cyclicNames(parentMap);

        categoryMap.forEach((name, node) -> {
            var parentName = parentMap.get(name);
            if (parentName != null && !cyclicNames.contains(name)) {
                categoryMap.get(parentName).getChildren().add(node);
            }
        });

        var roots = categoryMap.values().stream()
                .filter(node -> {
                    var name = node.getCategory().getMetadata().getName();
                    return !parentMap.containsKey(name) || cyclicNames.contains(name);
                })
                .collect(Collectors.toCollection(ArrayList::new));
        sortTree(roots);
        return roots;
    }

    private static Map<String, String> validParentMap(Map<String, CategoryTreeNode> categoryMap) {
        Map<String, String> parentMap = new LinkedHashMap<>();
        categoryMap.forEach((name, node) -> {
            var parentName = parentNameOf(node.getCategory());
            if (parentName != null && !Objects.equals(parentName, name) && categoryMap.containsKey(parentName)) {
                parentMap.put(name, parentName);
            }
        });
        return parentMap;
    }

    private static Set<String> cyclicNames(Map<String, String> parentMap) {
        Set<String> cyclicNames = new HashSet<>();
        for (String name : parentMap.keySet()) {
            var path = new ArrayList<String>();
            var visiting = new HashSet<String>();
            var current = name;
            while (current != null && parentMap.containsKey(current)) {
                if (!visiting.add(current)) {
                    cyclicNames.addAll(path.subList(path.indexOf(current), path.size()));
                    break;
                }
                path.add(current);
                current = parentMap.get(current);
            }
        }
        return cyclicNames;
    }

    private static void sortTree(List<CategoryTreeNode> nodes) {
        nodes.sort(Comparator.comparing(CategoryTreeNode::getCategory, defaultCategoryComparator()));
        nodes.forEach(node -> sortTree(node.getChildren()));
    }

    private static Comparator<Category> defaultCategoryComparator() {
        return Comparator.comparing(CategoryConsoleService::priorityOf)
                .thenComparing(
                        CategoryConsoleService::creationTimestampOf, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(category -> category.getMetadata().getName());
    }

    private static Instant creationTimestampOf(Category category) {
        return Optional.ofNullable(category.getMetadata())
                .map(MetadataOperator::getCreationTimestamp)
                .orElse(null);
    }

    private static int priorityOf(Category category) {
        return Optional.ofNullable(category.getSpec())
                .map(Category.CategorySpec::getPriority)
                .orElse(0);
    }

    private static String parentNameOf(Category category) {
        return normalize(Optional.ofNullable(category.getSpec())
                .map(Category.CategorySpec::getParent)
                .orElse(null));
    }

    private static boolean isDescendant(String candidateName, String ancestorName, Map<String, Category> categoryMap) {
        var current = candidateName;
        var visited = new HashSet<String>();
        while (current != null) {
            if (Objects.equals(current, ancestorName)) {
                return true;
            }
            if (!visited.add(current)) {
                throw new ServerWebInputException("Target parent has a cyclic parent chain.");
            }
            current = Optional.ofNullable(categoryMap.get(current))
                    .map(CategoryConsoleService::parentNameOf)
                    .orElse(null);
        }
        return false;
    }

    private static List<Category> siblings(List<Category> categories, String parentName, String excludingName) {
        return categories.stream()
                .filter(category -> !Objects.equals(category.getMetadata().getName(), excludingName))
                .filter(category -> Objects.equals(parentNameOf(category), parentName))
                .sorted(defaultCategoryComparator())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static int indexOf(List<Category> categories, String name) {
        for (int i = 0; i < categories.size(); i++) {
            if (Objects.equals(categories.get(i).getMetadata().getName(), name)) {
                return i;
            }
        }
        return -1;
    }

    private static void assignPriorities(List<Category> siblings, String parentName) {
        for (int i = 0; i < siblings.size(); i++) {
            var category = siblings.get(i);
            if (category.getSpec() == null) {
                category.setSpec(new Category.CategorySpec());
            }
            category.getSpec().setParent(parentName);
            category.getSpec().setPriority(i);
        }
    }

    private static boolean hasHierarchyChanged(Category category, HierarchyState original) {
        if (original == null) {
            return false;
        }
        return !Objects.equals(parentNameOf(category), original.parentName())
                || priorityOf(category) != original.priority();
    }

    private static String normalize(String value) {
        return org.springframework.util.StringUtils.hasText(value) ? value : null;
    }

    private record HierarchyState(String parentName, int priority) {}
}
