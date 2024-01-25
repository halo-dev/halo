package run.halo.app.theme.router;

import java.util.function.Predicate;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListOptions;

/**
 * The reactive query post predicate resolver.
 *
 * @author guqing
 * @since 2.9.0
 */
public interface ReactiveQueryPostPredicateResolver {

    Mono<Predicate<Post>> getPredicate();

    Mono<ListOptions> getListOptions();
}
