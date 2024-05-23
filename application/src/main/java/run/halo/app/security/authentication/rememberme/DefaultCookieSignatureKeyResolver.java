package run.halo.app.security.authentication.rememberme;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.security.authentication.login.CryptoService;

@Component
@RequiredArgsConstructor
public class DefaultCookieSignatureKeyResolver implements CookieSignatureKeyResolver {
    private final CryptoService cryptoService;

    @Override
    public Mono<String> resolveSigningKey() {
        return cryptoService.readPrivateKey()
            .map(String::new);
    }
}
