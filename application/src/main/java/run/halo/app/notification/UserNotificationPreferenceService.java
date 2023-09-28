package run.halo.app.notification;

import reactor.core.publisher.Mono;

/**
 * User notification preference service.
 *
 * @author guqing
 * @since 2.10.0
 */
public interface UserNotificationPreferenceService {

    Mono<UserNotificationPreference> getByUser(String username);
}
