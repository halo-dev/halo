package run.halo.app.theme.router;

import java.security.Principal;
import java.util.Objects;
import java.util.function.Predicate;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.infra.AnonymousUserConst;

/**
 * The default implementation of {@link ReactiveQueryPostPredicateResolver}.
 *
 * @author guqing
 * @since 2.9.0
 */
@Component
public class DefaultQueryPostPredicateResolver implements ReactiveQueryPostPredicateResolver {

    @Override
    public Mono<Predicate<Post>> getPredicate() {
        Predicate<Post> predicate = post -> post.isPublished()
            && !ExtensionUtil.isDeleted(post)
            && Objects.equals(false, post.getSpec().getDeleted());
        Predicate<Post> visiblePredicate =
            post -> Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible());
        return currentUserName()
            .map(username -> predicate.and(
                visiblePredicate.or(post -> username.equals(post.getSpec().getOwner())))
            )
            .defaultIfEmpty(predicate.and(visiblePredicate));
    }

    Mono<String> currentUserName() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName)
            .filter(name -> !AnonymousUserConst.isAnonymousUser(name));
    }
}
