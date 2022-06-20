package run.halo.app.core.extension.service;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;

public interface UserService {

    Mono<User> getUser(String username);

    Mono<Void> updatePassword(String username, String newPassword);

}
