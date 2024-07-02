package run.halo.app.content.impl;

import static run.halo.app.extension.index.query.QueryFactory.equal;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.CategoryService;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.selector.FieldSelector;

@Component
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final ReactiveExtensionClient client;

    @Override
    public Flux<Category> listChildren(@NonNull String categoryName) {
        return client.fetch(Category.class, categoryName)
            .expand(category -> {
                var children = category.getSpec().getChildren();
                if (children == null || children.isEmpty()) {
                    return Mono.empty();
                }
                return Flux.fromIterable(children)
                    .flatMap(name -> client.fetch(Category.class, name))
                    .filter(this::isNotIndependent);
            });
    }

    @Override
    public Mono<Category> getParentByName(@NonNull String name) {
        if (StringUtils.isBlank(name)) {
            return Mono.empty();
        }
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(
            equal("spec.children", name)
        ));
        return client.listBy(Category.class, listOptions,
                PageRequestImpl.of(1, 1, defaultSort())
            )
            .flatMap(result -> Mono.justOrEmpty(ListResult.first(result)));
    }

    @Override
    public Mono<Boolean> isCategoryHidden(@NonNull String categoryName) {
        return client.fetch(Category.class, categoryName)
            .expand(category -> getParentByName(category.getMetadata().getName()))
            .filter(category -> category.getSpec().isHideFromList())
            .hasElements();
    }

    static Sort defaultSort() {
        return Sort.by(Sort.Order.desc("spec.priority"),
            Sort.Order.desc("metadata.creationTimestamp"),
            Sort.Order.desc("metadata.name"));
    }

    private boolean isNotIndependent(Category category) {
        return !category.getSpec().isPreventParentPostCascadeQuery();
    }
}
