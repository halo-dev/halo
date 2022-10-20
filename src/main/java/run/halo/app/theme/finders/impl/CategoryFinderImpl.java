package run.halo.app.theme.finders.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import run.halo.app.core.extension.Category;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.vo.CategoryTreeVo;
import run.halo.app.theme.finders.vo.CategoryVo;

/**
 * A default implementation of {@link CategoryFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Finder("categoryFinder")
public class CategoryFinderImpl implements CategoryFinder {
    private final ReactiveExtensionClient client;

    public CategoryFinderImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public CategoryVo getByName(String name) {
        return client.fetch(Category.class, name)
            .map(CategoryVo::from)
            .block();
    }

    @Override
    public List<CategoryVo> getByNames(List<String> names) {
        if (names == null) {
            return List.of();
        }
        return names.stream().map(this::getByName)
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public ListResult<CategoryVo> list(Integer page, Integer size) {
        return client.list(Category.class, null,
                defaultComparator(), pageNullSafe(page), sizeNullSafe(size))
            .map(list -> {
                List<CategoryVo> categoryVos = list.stream()
                    .map(CategoryVo::from)
                    .collect(Collectors.toList());
                return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    categoryVos);
            })
            .block();
    }

    @Override
    public List<CategoryVo> listAll() {
        return client.list(Category.class, null, defaultComparator())
            .map(CategoryVo::from)
            .collectList()
            .block();
    }

    @Override
    public List<CategoryTreeVo> listAsTree() {
        List<CategoryVo> categoryVos = listAll();
        Map<String, CategoryTreeVo> nameIdentityMap = categoryVos.stream()
            .map(CategoryTreeVo::from)
            .collect(Collectors.toMap(categoryVo -> categoryVo.getMetadata().getName(),
                Function.identity()));

        nameIdentityMap.forEach((name, value) -> {
            List<String> children = value.getSpec().getChildren();
            if (children != null) {
                for (String child : children) {
                    CategoryTreeVo childNode = nameIdentityMap.get(child);
                    childNode.setParentName(name);
                }
            }
        });
        return listToTree(nameIdentityMap.values());
    }

    static List<CategoryTreeVo> listToTree(Collection<CategoryTreeVo> list) {
        Map<String, List<CategoryTreeVo>> parentNameIdentityMap = list.stream()
            .filter(categoryTreeVo -> categoryTreeVo.getParentName() != null)
            .collect(Collectors.groupingBy(CategoryTreeVo::getParentName));

        list.forEach(node -> {
            // sort children
            List<CategoryTreeVo> children =
                parentNameIdentityMap.getOrDefault(node.getMetadata().getName(), List.of())
                    .stream()
                    .sorted(defaultTreeNodeComparator())
                    .toList();
            node.setChildren(children);
        });
        return list.stream()
            .filter(v -> v.getParentName() == null)
            .sorted(defaultTreeNodeComparator())
            .collect(Collectors.toList());
    }

    static Comparator<CategoryTreeVo> defaultTreeNodeComparator() {
        Function<CategoryTreeVo, Integer> priority =
            category -> Objects.requireNonNullElse(category.getSpec().getPriority(), 0);
        Function<CategoryTreeVo, Instant> creationTimestamp =
            category -> category.getMetadata().getCreationTimestamp();
        Function<CategoryTreeVo, String> name =
            category -> category.getMetadata().getName();
        return Comparator.comparing(priority)
            .thenComparing(creationTimestamp)
            .thenComparing(name);
    }

    static Comparator<Category> defaultComparator() {
        Function<Category, Integer> priority =
            category -> Objects.requireNonNullElse(category.getSpec().getPriority(), 0);
        Function<Category, Instant> creationTimestamp =
            category -> category.getMetadata().getCreationTimestamp();
        Function<Category, String> name =
            category -> category.getMetadata().getName();
        return Comparator.comparing(priority)
            .thenComparing(creationTimestamp)
            .thenComparing(name)
            .reversed();
    }

    int pageNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 1);
    }

    int sizeNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 10);
    }
}
