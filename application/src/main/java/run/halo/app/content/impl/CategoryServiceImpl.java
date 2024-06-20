package run.halo.app.content.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.CategoryService;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.ReactiveExtensionClient;

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

    private boolean isNotIndependent(Category category) {
        return !category.getSpec().isPreventParentPostCascadeQuery();
    }
}
