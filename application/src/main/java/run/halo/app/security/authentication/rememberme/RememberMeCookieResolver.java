package run.halo.app.security.authentication.rememberme;

import java.time.Duration;
import org.springframework.http.HttpCookie;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebExchange;

public interface RememberMeCookieResolver {

    @Nullable
    HttpCookie resolveRememberMeCookie(ServerWebExchange exchange);

    void setRememberMeCookie(ServerWebExchange exchange, String value);

    void expireCookie(ServerWebExchange exchange);

    String getCookieName();

    Duration getCookieMaxAge();
}
