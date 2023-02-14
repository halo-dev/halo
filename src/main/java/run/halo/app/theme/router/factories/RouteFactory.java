package run.halo.app.theme.router.factories;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface RouteFactory {
    RouterFunction<ServerResponse> create(String pattern);

    default Mono<Integer> configuredPageSize(
        SystemConfigurableEnvironmentFetcher environmentFetcher) {
        return environmentFetcher.fetchPost()
            .map(p -> defaultIfNull(p.getTagPageSize(), ModelConst.DEFAULT_PAGE_SIZE));
    }

    default int pageNumInPathVariable(ServerRequest request) {
        String page = request.pathVariables().get("page");
        return NumberUtils.toInt(page, 1);
    }
}
