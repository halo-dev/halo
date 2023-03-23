package run.halo.app.core.extension.endpoint;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.ReactiveSortHandlerMethodArgumentResolver;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebExchange;

public interface SortResolver {

    SortResolver defaultInstance = new DefaultSortResolver();

    @NonNull
    Sort resolve(@NonNull ServerWebExchange exchange);

    class DefaultSortResolver extends ReactiveSortHandlerMethodArgumentResolver
        implements SortResolver {

        @Override
        @NonNull
        protected Sort getDefaultFromAnnotationOrFallback(@Nullable MethodParameter parameter) {
            return Sort.unsorted();
        }

        @Override
        public Sort resolve(ServerWebExchange exchange) {
            return resolveArgumentValue(null, null, exchange);
        }
    }
}
