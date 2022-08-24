package run.halo.app.theme.finders.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Category;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.SubscriberUtils;
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
