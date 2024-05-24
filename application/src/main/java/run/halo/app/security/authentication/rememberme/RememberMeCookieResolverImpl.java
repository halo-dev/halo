package run.halo.app.security.authentication.rememberme;

import java.time.Duration;
import lombok.Getter;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.infra.properties.HaloProperties;

@Getter
@Component
public class RememberMeCookieResolverImpl implements RememberMeCookieResolver {
    public static final String SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY = "remember-me";

    private final String cookieName = SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY;

    private final Duration cookieMaxAge;

    public RememberMeCookieResolverImpl(HaloProperties haloProperties) {
        this.cookieMaxAge = haloProperties.getSecurity().getRememberMe().getTokenValidity();
    }

    @Override
    @Nullable
    public HttpCookie resolveRememberMeCookie(ServerWebExchange exchange) {
        return exchange.getRequest().getCookies().getFirst(getCookieName());
    }

    @Override
    public void setRememberMeCookie(ServerWebExchange exchange, String value) {
        Assert.notNull(value, "'value' is required");
        exchange.getResponse().getCookies()
            .set(getCookieName(), initCookie(exchange, value).build());
    }

    @Override
    public void expireCookie(ServerWebExchange exchange) {
        ResponseCookie cookie = initCookie(exchange, "").maxAge(0).build();
        exchange.getResponse().getCookies().set(this.cookieName, cookie);
    }

    private ResponseCookie.ResponseCookieBuilder initCookie(ServerWebExchange exchange,
        String value) {
        return ResponseCookie.from(this.cookieName, value)
            .path(exchange.getRequest().getPath().contextPath().value() + "/")
            .maxAge(getCookieMaxAge())
            .httpOnly(true)
            .secure("https".equalsIgnoreCase(exchange.getRequest().getURI().getScheme()))
            .sameSite("Lax");
    }
}
