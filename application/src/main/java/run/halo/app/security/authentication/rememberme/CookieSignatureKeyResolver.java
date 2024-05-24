package run.halo.app.security.authentication.rememberme;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface CookieSignatureKeyResolver {
    Mono<String> resolveSigningKey();
}
