package run.halo.app.theme.router;

import java.util.function.Predicate;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;

/**
 * The reactive query post predicate resolver.
 *
 * @author guqing
 * @since 2.9.0
 */
public interface ReactiveQueryPostPredicateResolver {

    Mono<Predicate<Post>> getPredicate();
}
