package run.halo.app.security.device;

import java.time.Duration;
import org.springframework.http.HttpCookie;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebExchange;

public interface DeviceCookieResolver {
    @Nullable
    HttpCookie resolveCookie(ServerWebExchange exchange);

    void setCookie(ServerWebExchange exchange, String value);

    void expireCookie(ServerWebExchange exchange);

    String getCookieName();

    Duration getCookieMaxAge();
}
