package run.halo.app.content;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import run.halo.app.core.extension.content.Category;

public interface CategoryService {

    Flux<Category> listChildren(@NonNull String categoryName);
}
