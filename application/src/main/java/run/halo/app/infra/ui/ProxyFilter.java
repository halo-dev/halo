package run.halo.app.infra.ui;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import run.halo.app.infra.properties.ProxyProperties;

@Slf4j
public class ProxyFilter implements WebFilter {

    private final ProxyProperties proxyProperties;

    private final ServerWebExchangeMatcher requestMatcher;

    private final WebClient webClient;

    public ProxyFilter(ProxyProperties proxyProperties, String... patterns) {
        this.proxyProperties = proxyProperties;
        var requestMatcher = ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, patterns);
        requestMatcher = new AndServerWebExchangeMatcher(requestMatcher,
            new NegatedServerWebExchangeMatcher(new WebSocketServerWebExchangeMatcher()));
        this.requestMatcher = requestMatcher;
        this.webClient = WebClient.create(proxyProperties.getEndpoint().toString());
        log.debug("Initialized ProxyFilter to proxy {} to endpoint {}",
            java.util.Arrays.toString(patterns),
            proxyProperties.getEndpoint());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return requestMatcher.matches(exchange)
            .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
            .filter(matchResult -> isHtmlRequest(exchange))
            .switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
            .map(matchResult -> getProxyUri(exchange))
            .doOnNext(uri -> {
                if (log.isTraceEnabled()) {
                    log.trace("Proxy {} to {}", uri, proxyProperties.getEndpoint());
                }
            })
            .flatMap(uri -> webClient.get()
                .uri(uri)
                .headers(httpHeaders -> httpHeaders.addAll(exchange.getRequest().getHeaders()))
                .exchangeToMono(clientResponse -> {
                    var response = exchange.getResponse();
                    var httpStatusCode = clientResponse.statusCode();
                    // set headers
                    var httpHeaders = clientResponse.headers().asHttpHeaders();
                    response.getHeaders().putAll(httpHeaders);
                    // set cookies
                    response.getCookies().putAll(clientResponse.cookies());
                    // set status code
                    response.setStatusCode(httpStatusCode);
                    var contentLength = clientResponse.headers().contentLength().orElse(0L);
                    if (httpStatusCode.is3xxRedirection()
                        || httpStatusCode.equals(HttpStatus.NO_CONTENT)
                        || contentLength == 0) {
                        return Mono.empty();
                    }
                    var body = clientResponse.bodyToFlux(DataBuffer.class);
                    return response.writeWith(body);
                }));
    }

    private boolean isHtmlRequest(ServerWebExchange exchange) {
        var acceptHeaders = exchange.getRequest().getHeaders().getAccept();
        if (acceptHeaders.isEmpty()) {
            return true;
        }
        return acceptHeaders.stream()
            .anyMatch(mediaType -> mediaType.isCompatibleWith(MediaType.TEXT_HTML));
    }

    private String getProxyUri(ServerWebExchange exchange) {
        var requestPath = exchange.getRequest().getPath().pathWithinApplication().value();
        return UriComponentsBuilder.fromUriString(getUiEntryPath(requestPath))
            .queryParams(exchange.getRequest().getQueryParams())
            .build()
            .toUriString();
    }

    private String getUiEntryPath(String requestPath) {
        if (requestPath.startsWith("/console")) {
            return "/console";
        }
        if (requestPath.startsWith("/uc")) {
            return "/uc";
        }
        return requestPath;
    }
}
