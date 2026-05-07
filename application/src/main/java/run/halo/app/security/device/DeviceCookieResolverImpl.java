package run.halo.app.security.device;

import java.time.Duration;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Getter
@Component
class DeviceCookieResolverImpl implements DeviceCookieResolver {
    public static final String DEVICE_COOKIE_KEY = "device_id";

    private final String cookieName = DEVICE_COOKIE_KEY;

    private final Duration cookieMaxAge = Duration.ofDays(100);

    @Override
    @Nullable
    public HttpCookie resolveCookie(ServerWebExchange exchange) {
        return exchange.getRequest().getCookies().getFirst(getCookieName());
    }

    @Override
    public void setCookie(ServerWebExchange exchange, String value) {
        Assert.notNull(value, "'value' is required");
        exchange.getResponse()
                .beforeCommit(() -> Mono.fromRunnable(() -> {
                    var deviceCookie = initCookie(exchange, value).build();
                    exchange.getResponse().addCookie(deviceCookie);
                }));
    }

    @Override
    public void expireCookie(ServerWebExchange exchange) {
        ResponseCookie cookie = initCookie(exchange, "").maxAge(0).build();
        exchange.getResponse().getCookies().set(this.cookieName, cookie);
    }

    private ResponseCookie.ResponseCookieBuilder initCookie(ServerWebExchange exchange, String value) {
        return ResponseCookie.from(this.cookieName, value)
                .path(exchange.getRequest().getPath().contextPath().value() + "/")
                .maxAge(getCookieMaxAge())
                .httpOnly(true)
                .secure("https".equalsIgnoreCase(exchange.getRequest().getURI().getScheme()))
                .sameSite("Lax");
    }
}
