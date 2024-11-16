package run.halo.app.core.user.service;

import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;

/**
 * User post-creating handler.
 *
 * @author johnniang
 * @since 2.20.8
 */
public interface UserPostCreatingHandler extends ExtensionPoint {

    /**
     * Do something after creating user.
     *
     * @param user create user.
     * @return {@code Mono.empty()} if handling successfully.
     */
    Mono<Void> postCreating(User user);

}
