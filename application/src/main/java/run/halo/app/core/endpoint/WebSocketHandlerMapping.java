package run.halo.app.core.endpoint;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.handler.AbstractHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import reactor.core.publisher.Mono;
import run.halo.app.console.WebSocketUtils;

public class WebSocketHandlerMapping extends AbstractHandlerMapping
    implements WebSocketEndpointManager, InitializingBean {

    private final BiMap<PathPattern, WebSocketEndpoint> endpointMap;

    private final ReadWriteLock rwLock;

    public WebSocketHandlerMapping() {
        this.endpointMap = HashBiMap.create();
        this.rwLock = new ReentrantReadWriteLock();
    }

    @Override
    @NonNull
    public Mono<WebSocketHandler> getHandlerInternal(ServerWebExchange exchange) {
        var request = exchange.getRequest();
        if (!HttpMethod.GET.equals(request.getMethod())
            || !WebSocketUtils.isWebSocketUpgrade(request.getHeaders())) {
            // skip getting handler if the request is not a WebSocket.
            return Mono.empty();
        }

        var lock = rwLock.readLock();
        lock.lock();
        try {
            // Refer to org.springframework.web.reactive.handler.AbstractUrlHandlerMapping
            // .lookupHandler
            var pathContainer = request.getPath().pathWithinApplication();
            List<PathPattern> matches = null;
            for (var pattern : this.endpointMap.keySet()) {
                if (pattern.matches(pathContainer)) {
                    if (matches == null) {
                        matches = new ArrayList<>();
                    }
                    matches.add(pattern);
                }
            }
            if (matches == null) {
                return Mono.empty();
            }

            if (matches.size() > 1) {
                matches.sort(PathPattern.SPECIFICITY_COMPARATOR);
            }

            var pattern = matches.get(0);
            exchange.getAttributes().put(BEST_MATCHING_PATTERN_ATTRIBUTE, pattern);

            var handler = endpointMap.get(pattern).handler();
            exchange.getAttributes().put(BEST_MATCHING_HANDLER_ATTRIBUTE, handler);

            ServerRequestObservationContext.findCurrent(exchange.getAttributes())
                .ifPresent(context -> context.setPathPattern(pattern.toString()));

            var pathWithinMapping = pattern.extractPathWithinPattern(pathContainer);
            exchange.getAttributes().put(PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, pathWithinMapping);

            var matchInfo = pattern.matchAndExtract(pathContainer);
            Assert.notNull(matchInfo, "Expect a match");
            exchange.getAttributes()
                .put(URI_TEMPLATE_VARIABLES_ATTRIBUTE, matchInfo.getUriVariables());
            return Mono.just(handler);
        } catch (Exception e) {
            return Mono.error(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void register(Collection<WebSocketEndpoint> endpoints) {
        if (CollectionUtils.isEmpty(endpoints)) {
            return;
        }
        var lock = rwLock.writeLock();
        lock.lock();
        try {
            endpoints.forEach(endpoint -> {
                var urlPath = endpoint.urlPath();
                urlPath = StringUtils.prependIfMissing(urlPath, "/");
                var groupVersion = endpoint.groupVersion();
                var parser = getPathPatternParser();
                var pattern = parser.parse("/apis/" + groupVersion + urlPath);
                endpointMap.put(pattern, endpoint);
            });
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void unregister(Collection<WebSocketEndpoint> endpoints) {
        if (CollectionUtils.isEmpty(endpoints)) {
            return;
        }
        var lock = rwLock.writeLock();
        lock.lock();
        try {
            BiMap<WebSocketEndpoint, PathPattern> inverseMap = endpointMap.inverse();
            endpoints.forEach(inverseMap::remove);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void afterPropertiesSet() {
        var endpoints = obtainApplicationContext().getBeanProvider(WebSocketEndpoint.class)
            .orderedStream()
            .toList();
        register(endpoints);
    }

    BiMap<PathPattern, WebSocketEndpoint> getEndpointMap() {
        return endpointMap;
    }
}
