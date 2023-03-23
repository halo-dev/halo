package run.halo.app.theme.router.factories;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.function.Function;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface RouteFactory {
    RouterFunction<ServerResponse> create(String pattern);

    default Mono<Integer> configuredPageSize(
        SystemConfigurableEnvironmentFetcher environmentFetcher,
        Function<SystemSetting.Post, Integer> mapper) {
        return environmentFetcher.fetchPost()
            .map(p -> defaultIfNull(mapper.apply(p), ModelConst.DEFAULT_PAGE_SIZE));
    }

    default int pageNumInPathVariable(ServerRequest request) {
        String page = request.pathVariables().get("page");
        return NumberUtils.toInt(page, 1);
    }
}
