package run.halo.app.content.impl;

import static run.halo.app.extension.index.query.Queries.equal;

import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.CategoryService;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;

@Component
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final ReactiveExtensionClient client;

    @Override
    public Flux<Category> listChildren(String categoryName) {
        if (StringUtils.isBlank(categoryName)) {
            return Flux.empty();
        }
        return client.fetch(Category.class, categoryName).flatMapMany(category -> {
            var name = category.getMetadata().getName();
            if (StringUtils.isBlank(name)) {
                return Flux.just(category);
            }
            return Flux.concat(Mono.just(category), listDescendants(name, false, new HashSet<>(Set.of(name))));
        });
    }

    @Override
    public Flux<Category> listDescendants(String categoryName) {
        if (StringUtils.isBlank(categoryName)) {
            return Flux.empty();
        }
        return listDescendants(categoryName, true, new HashSet<>(Set.of(categoryName)));
    }

    @Override
    public Mono<Category> getParentByName(String name) {
        if (StringUtils.isBlank(name)) {
            return Mono.empty();
        }
        return client.fetch(Category.class, name)
                .mapNotNull(CategoryServiceImpl::parentName)
                .flatMap(parentName -> client.fetch(Category.class, parentName));
    }

    @Override
    public Mono<Boolean> isCategoryHidden(String categoryName) {
        if (StringUtils.isBlank(categoryName)) {
            return Mono.just(false);
        }
        return client.fetch(Category.class, categoryName)
                .flatMapMany(category -> listSelfAndAncestors(category, new HashSet<>()))
                .filter(category -> category.getSpec().isHideFromList())
                .hasElements();
    }

    private Flux<Category> listDescendants(String parentName, boolean includeIndependent, Set<String> visited) {
        return listDirectChildren(parentName).concatMap(child -> {
            var childName = child.getMetadata().getName();
            if (StringUtils.isBlank(childName) || !visited.add(childName)) {
                return Flux.empty();
            }
            if (!includeIndependent && !isNotIndependent(child)) {
                return Flux.empty();
            }
            return Flux.concat(Mono.just(child), listDescendants(childName, includeIndependent, visited));
        });
    }

    private Flux<Category> listDirectChildren(String parentName) {
        var listOptions = ListOptions.builder()
                .fieldQuery(equal("spec.parent", parentName))
                .build();
        return client.listAll(Category.class, listOptions, defaultSort());
    }

    private Flux<Category> listSelfAndAncestors(Category category, Set<String> visited) {
        var name = category.getMetadata().getName();
        if (StringUtils.isBlank(name) || !visited.add(name)) {
            return Flux.empty();
        }
        var parentName = parentName(category);
        if (StringUtils.isBlank(parentName)) {
            return Flux.just(category);
        }
        return Flux.concat(
                Mono.just(category),
                client.fetch(Category.class, parentName).flatMapMany(parent -> listSelfAndAncestors(parent, visited)));
    }

    static Sort defaultSort() {
        return Sort.by(
                Sort.Order.desc("spec.priority"),
                Sort.Order.desc("metadata.creationTimestamp"),
                Sort.Order.desc("metadata.name"));
    }

    private boolean isNotIndependent(Category category) {
        return !category.getSpec().isPreventParentPostCascadeQuery();
    }

    private static String parentName(Category category) {
        var spec = category.getSpec();
        return spec == null ? null : spec.getParent();
    }
}
