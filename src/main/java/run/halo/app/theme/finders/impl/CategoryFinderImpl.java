package run.halo.app.theme.finders.impl;

import java.util.Comparator;
import java.util.List;
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
        Comparator<Category> comparator =
            Comparator.comparing(category -> category.getMetadata().getCreationTimestamp());
        Mono<ListResult<CategoryVo>> mono = client.list(Category.class, null,
                comparator.reversed(), Math.max(page - 1, 0), size)
            .map(list -> {
                List<CategoryVo> categoryVos = list.stream()
                    .map(CategoryVo::from)
                    .collect(Collectors.toList());
                return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    categoryVos);
            });
        return SubscriberUtils.subscribe(mono);
    }
}
