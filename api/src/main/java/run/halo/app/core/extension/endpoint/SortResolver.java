package run.halo.app.core.extension.endpoint;

import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.ReactiveSortHandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

public interface SortResolver {

    SortResolver defaultInstance = new DefaultSortResolver();

    Sort resolve(ServerWebExchange exchange);

    class DefaultSortResolver extends ReactiveSortHandlerMethodArgumentResolver
        implements SortResolver {

        @Override
        protected Sort getDefaultFromAnnotationOrFallback(@Nullable MethodParameter parameter) {
            return Sort.unsorted();
        }

        @Override
        public Sort resolve(ServerWebExchange exchange) {
            return resolveArgumentValue(null, null, exchange);
        }
    }
}
