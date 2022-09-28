package run.halo.app.theme.router.strategy;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.util.UriUtils;

public enum PermalinkPredicates {
    ;

    public static RequestPredicate get(String permalink) {
        return method(HttpMethod.GET).and(path(UriUtils.decode(permalink, UTF_8)));
    }
}
