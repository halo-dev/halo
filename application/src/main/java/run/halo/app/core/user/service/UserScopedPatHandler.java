package run.halo.app.core.user.service;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface UserScopedPatHandler {

    Mono<ServerResponse> create(ServerRequest request);

    Mono<ServerResponse> list(ServerRequest request);

    Mono<ServerResponse> get(ServerRequest request);

    Mono<ServerResponse> revoke(ServerRequest request);

    Mono<ServerResponse> delete(ServerRequest request);

    Mono<ServerResponse> restore(ServerRequest request);
}
