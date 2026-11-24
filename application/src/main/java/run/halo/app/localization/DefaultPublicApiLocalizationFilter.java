package run.halo.app.localization;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/** Resolves an explicitly requested public API language without intercepting other endpoints. */
@Component
@RequiredArgsConstructor
public class DefaultPublicApiLocalizationFilter implements PublicApiLocalizationFilter {
    private final PublicApiLocalizationResolver resolver;

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        return Mono.deferContextual(view -> {
            if (view.hasKey(LocalizationContext.class)) {
                return handle(request, next, view.get(LocalizationContext.class));
            }
            return resolver.resolve(request.queryParam("locale").orElse(null))
                    .map(Optional::of)
                    .defaultIfEmpty(Optional.empty())
                    .flatMap(context -> {
                        if (context.isEmpty()) {
                            return next.handle(request);
                        }
                        return handle(request, next, context.get());
                    });
        });
    }

    private Mono<ServerResponse> handle(
            ServerRequest request, HandlerFunction<ServerResponse> next, LocalizationContext context) {
        return next.handle(request)
                .map(response -> (ServerResponse) new ContextualResponse(response, context))
                .contextWrite(current -> current.put(LocalizationContext.class, context));
    }

    private record ContextualResponse(ServerResponse delegate, LocalizationContext localization)
            implements ServerResponse {
        @Override
        public org.springframework.http.HttpStatusCode statusCode() {
            return delegate.statusCode();
        }

        @Override
        public org.springframework.http.HttpHeaders headers() {
            return delegate.headers();
        }

        @Override
        public org.springframework.util.MultiValueMap<String, org.springframework.http.ResponseCookie> cookies() {
            return delegate.cookies();
        }

        @Override
        public Mono<Void> writeTo(org.springframework.web.server.ServerWebExchange exchange, Context context) {
            return delegate.writeTo(exchange, context)
                    .contextWrite(current -> current.put(LocalizationContext.class, localization));
        }
    }
}
