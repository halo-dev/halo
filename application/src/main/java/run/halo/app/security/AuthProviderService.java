package run.halo.app.security;

import java.util.List;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.AuthProvider;

/**
 * A service for {@link AuthProvider}.
 *
 * @author guqing
 * @since 2.4.0
 */
public interface AuthProviderService {

    Mono<AuthProvider> enable(String name);

    Mono<AuthProvider> disable(String name);

    Mono<List<ListedAuthProvider>> listAll();
}
