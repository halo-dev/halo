package run.halo.app.core.user.service;

import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;

/**
 * User pre-creating handler.
 *
 * @author johnniang
 * @since 2.20.8
 */
public interface UserPreCreatingHandler extends ExtensionPoint {

    /**
     * Do something before user creating.
     *
     * @param user modifiable user detail
     * @return {@code Mono.empty()} if handling successfully.
     */
    Mono<Void> preCreating(User user);

}
