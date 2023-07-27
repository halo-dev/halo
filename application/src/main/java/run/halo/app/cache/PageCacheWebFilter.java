package run.halo.app.cache;

import static java.nio.ByteBuffer.allocateDirect;
import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static run.halo.app.infra.AnonymousUserConst.isAnonymousUser;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class PageCacheWebFilter implements WebFilter, Ordered {

    public static final String REQUEST_TO_CACHE = "RequestCacheWebFilterToCache";

    public static final String CACHE_NAME = "page";

    private final Cache cache;

    public PageCacheWebFilter(CacheManager cacheManager) {
        this.cache = cacheManager.getCache(CACHE_NAME);
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName)
            .filter(name -> isAnonymousUser(name) && requestCacheable(exchange.getRequest()))
            .switchIfEmpty(Mono.defer(() -> chain.filter(exchange).then(Mono.empty())))
            .flatMap(name -> {
                var cacheKey = generateCacheKey(exchange.getRequest());
                var cachedResponse = cache.get(cacheKey, CachedResponse.class);
                if (cachedResponse != null) {
                    // cache hit, then write the cached response
                    return writeCachedResponse(exchange.getResponse(), cachedResponse);
                }
                // decorate the ServerHttpResponse to cache the response
                var decoratedExchange = exchange.mutate()
                    .response(new CacheResponseDecorator(exchange, cacheKey))
                    .build();
                return chain.filter(decoratedExchange);
            });
    }

    private boolean requestCacheable(ServerHttpRequest request) {
        return HttpMethod.GET.equals(request.getMethod())
            && !hasRequestBody(request)
            && enableCacheByCacheControl(request.getHeaders());
    }

    private boolean enableCacheByCacheControl(HttpHeaders headers) {
        return headers.getOrEmpty(CACHE_CONTROL)
            .stream()
            .noneMatch(cacheControl ->
                "no-store".equals(cacheControl) || "private".equals(cacheControl));
    }

    private boolean responseCacheable(ServerWebExchange exchange) {
        var response = exchange.getResponse();
        if (!MediaType.TEXT_HTML.equals(response.getHeaders().getContentType())) {
            return false;
        }
        var statusCode = response.getStatusCode();
        if (statusCode == null || !statusCode.isSameCodeAs(HttpStatus.OK)) {
            return false;
        }
        return exchange.getAttributeOrDefault(REQUEST_TO_CACHE, false);
    }

    private static boolean hasRequestBody(ServerHttpRequest request) {
        return request.getHeaders().getContentLength() > 0;
    }

    private String generateCacheKey(ServerHttpRequest request) {
        return request.getURI().toASCIIString();
    }

    @Override
    public int getOrder() {
        // The filter should be after org.springframework.security.web.server.WebFilterChainProxy
        return Ordered.LOWEST_PRECEDENCE;
    }

    private Mono<Void> writeCachedResponse(ServerHttpResponse response,
        CachedResponse cachedResponse) {
        response.setStatusCode(cachedResponse.statusCode());
        response.getHeaders().clear();
        response.getHeaders().addAll(cachedResponse.headers());
        var body = Flux.fromIterable(cachedResponse.body())
            .map(byteBuffer -> response.bufferFactory().wrap(byteBuffer));
        return response.writeWith(body);
    }

    class CacheResponseDecorator extends ServerHttpResponseDecorator {

        private final ServerWebExchange exchange;

        private final String cacheKey;

        public CacheResponseDecorator(ServerWebExchange exchange, String cacheKey) {
            super(exchange.getResponse());
            this.exchange = exchange;
            this.cacheKey = cacheKey;
        }

        @Override
        @NonNull
        public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
            if (responseCacheable(exchange)) {
                var response = getDelegate();
                body = Flux.from(body)
                    .map(dataBuffer -> {
                        var byteBuffer = allocateDirect(dataBuffer.readableByteCount());
                        dataBuffer.toByteBuffer(byteBuffer);
                        DataBufferUtils.release(dataBuffer);
                        return byteBuffer.asReadOnlyBuffer();
                    })
                    .collectSortedList()
                    .doOnSuccess(byteBuffers -> {
                        var headers = new HttpHeaders();
                        headers.addAll(response.getHeaders());
                        var cachedResponse = new CachedResponse(response.getStatusCode(),
                            headers,
                            byteBuffers,
                            Instant.now());
                        cache.put(cacheKey, cachedResponse);
                    })
                    .flatMapMany(Flux::fromIterable)
                    .map(byteBuffer -> response.bufferFactory().wrap(byteBuffer));
            }
            // write the response
            return super.writeWith(body);
        }
    }
}
