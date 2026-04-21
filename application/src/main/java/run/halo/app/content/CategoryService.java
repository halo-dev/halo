package run.halo.app.content;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Category;

public interface CategoryService {

    Flux<Category> listChildren(String categoryName);

    Mono<Category> getParentByName(String categoryName);

    Mono<Boolean> isCategoryHidden(String categoryName);
}
