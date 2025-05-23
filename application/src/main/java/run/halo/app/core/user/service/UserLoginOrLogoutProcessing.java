package run.halo.app.core.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.event.user.UserLoginEvent;
import run.halo.app.event.user.UserLogoutEvent;

/**
 * User login or logout processing service.
 *
 * @author lywq
 **/
@Component
@RequiredArgsConstructor
public class UserLoginOrLogoutProcessing {

    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public Mono<Void> loginProcessing(String username) {
        return userService.getUser(username)
            .doOnNext(user -> {
                eventPublisher.publishEvent(new UserLoginEvent(this, user));
            })
            .then();
    }

    public Mono<Void> logoutProcessing(String username) {
        return userService.getUser(username)
            .doOnNext(user -> {
                eventPublisher.publishEvent(new UserLogoutEvent(this, user));
            })
            .then();
    }

}
