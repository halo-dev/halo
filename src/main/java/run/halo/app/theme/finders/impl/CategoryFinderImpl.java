package run.halo.app.theme.finders.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Category;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.SubscriberUtils;
import run.halo.app.theme.finders.vo.CategoryTreeVo;
import run.halo.app.theme.finders.vo.CategoryVo;

/**
 * A default implementation of {@link CategoryFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("categoryFinder")
public class CategoryFinderImpl implements CategoryFinder {
    private final ReactiveExtensionClient client;

    public CategoryFinderImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public CategoryVo getByName(String name) {
        Mono<CategoryVo> mono = client.fetch(Category.class, name)
            .map(CategoryVo::from);
        return SubscriberUtils.subscribe(mono);
    }

    @Override
    public ListResult<CategoryVo> list(int page, int size) {
        Mono<ListResult<CategoryVo>> mono = client.list(Category.class, null,
                defaultComparator(), Math.max(page - 1, 0), size)
            .map(list -> {
                List<CategoryVo> categoryVos = list.stream()
                    .map(CategoryVo::from)
                    .collect(Collectors.toList());
                return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    categoryVos);
            });
        return SubscriberUtils.subscribe(mono);
    }

    @Override
    public List<CategoryVo> listAll() {
        Mono<List<CategoryVo>> listMono = client.list(Category.class, null, defaultComparator())
            .map(CategoryVo::from)
            .collectList();
        return SubscriberUtils.subscribe(listMono);
    }

    @Override
    public List<CategoryTreeVo> listAsTree() {
        List<CategoryVo> categoryVos = listAll();
        Map<String, CategoryVo> nameIdentityMap = categoryVos.stream()
            .collect(Collectors.toMap(CategoryVo::getName, Function.identity()));

        Map<String, CategoryTreeVo> treeVoMap = new HashMap<>();
        // populate parentName
        categoryVos.forEach(categoryVo -> {
            final String parentName = categoryVo.getName();
            treeVoMap.putIfAbsent(parentName, CategoryTreeVo.from(categoryVo));
            List<String> children = categoryVo.getChildren();
            if (CollectionUtils.isEmpty(children)) {
                return;
            }
            children.forEach(childrenName -> {
                CategoryVo childrenVo = nameIdentityMap.get(childrenName);
                CategoryTreeVo treeVo = CategoryTreeVo.from(childrenVo);
                treeVo.setParentName(parentName);
                treeVoMap.putIfAbsent(treeVo.getName(), treeVo);
            });
        });
        nameIdentityMap.clear();
        return listToTree(treeVoMap.values());
    }

    static List<CategoryTreeVo> listToTree(Collection<CategoryTreeVo> list) {
        Map<String, List<CategoryTreeVo>> nameIdentityMap = list.stream()
            .filter(item -> item.getParentName() != null)
            .collect(Collectors.groupingBy(CategoryTreeVo::getParentName));
        list.forEach(node -> node.setChildren(nameIdentityMap.get(node.getName())));
        return list.stream()
            .filter(v -> v.getParentName() == null)
            .collect(Collectors.toList());
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
}
