package run.halo.app.security.authentication.rememberme;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.security.authentication.CryptoService;

@Component
@RequiredArgsConstructor
public class DefaultCookieSignatureKeyResolver implements CookieSignatureKeyResolver {
    private final CryptoService cryptoService;

    @Override
    public Mono<String> resolveSigningKey() {
        return Mono.fromSupplier(cryptoService::getKeyId);
    }
}
