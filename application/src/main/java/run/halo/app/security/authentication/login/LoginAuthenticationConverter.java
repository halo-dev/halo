package run.halo.app.security.authentication.login;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerFormLoginAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.infra.exception.RateLimitExceededException;
import run.halo.app.infra.utils.IpAddressUtils;
import run.halo.app.security.authentication.CryptoService;

@Slf4j
public class LoginAuthenticationConverter extends ServerFormLoginAuthenticationConverter {

    private final CryptoService cryptoService;

    private final RateLimiterRegistry rateLimiterRegistry;

    public LoginAuthenticationConverter(CryptoService cryptoService,
        RateLimiterRegistry rateLimiterRegistry) {
        this.cryptoService = cryptoService;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return super.convert(exchange)
            // validate the password
            .<Authentication>flatMap(token -> {
                var credentials = (String) token.getCredentials();
                byte[] credentialsBytes;
                try {
                    credentialsBytes = Base64.getDecoder().decode(credentials);
                } catch (IllegalArgumentException e) {
                    // the credentials are not in valid Base64 scheme
                    return Mono.error(new BadCredentialsException("Invalid Base64 scheme."));
                }
                return cryptoService.decrypt(credentialsBytes)
                    .onErrorMap(InvalidEncryptedMessageException.class,
                        error -> new BadCredentialsException("Invalid credential.", error))
                    .map(decryptedCredentials -> new UsernamePasswordAuthenticationToken(
                        token.getPrincipal(),
                        new String(decryptedCredentials, UTF_8)));
            })
            .transformDeferred(createIpBasedRateLimiter(exchange))
            .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new);
    }

    private <T> RateLimiterOperator<T> createIpBasedRateLimiter(ServerWebExchange exchange) {
        var clientIp = IpAddressUtils.getClientIp(exchange.getRequest());
        var rateLimiter = rateLimiterRegistry.rateLimiter("authentication-from-ip-" + clientIp,
            "authentication");
        if (log.isDebugEnabled()) {
            var metrics = rateLimiter.getMetrics();
            log.debug(
                "Authentication with Rate Limiter: {}, available permissions: {}, number of "
                    + "waiting threads: {}",
                rateLimiter, metrics.getAvailablePermissions(),
                metrics.getNumberOfWaitingThreads());
        }
        return RateLimiterOperator.of(rateLimiter);
    }
}
