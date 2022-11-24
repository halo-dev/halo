package run.halo.app.console;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import run.halo.app.infra.properties.ConsoleProperties.ProxyProperties;
import run.halo.app.infra.properties.HaloProperties;

@Slf4j
public class ConsoleProxyFilter implements WebFilter {

    private final ProxyProperties proxyProperties;

    private final ServerWebExchangeMatcher consoleMatcher;

    private final WebClient webClient;

    public ConsoleProxyFilter(HaloProperties haloProperties) {
        this.proxyProperties = haloProperties.getConsole().getProxy();
        var consoleMatcher = ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/console/**");
        consoleMatcher = new AndServerWebExchangeMatcher(consoleMatcher,
            new MediaTypeServerWebExchangeMatcher(MediaType.TEXT_HTML));
        consoleMatcher = new AndServerWebExchangeMatcher(consoleMatcher,
            new NegatedServerWebExchangeMatcher(new WebSocketServerWebExchangeMatcher()));
        this.consoleMatcher = consoleMatcher;
        this.webClient = WebClient.create(proxyProperties.getEndpoint().toString());
        log.info("Initialized ConsoleProxyFilter to proxy console");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return consoleMatcher.matches(exchange)
            .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
            .switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
            .map(matchResult -> {
                var request = exchange.getRequest();
                return UriComponentsBuilder.fromUriString(
                        request.getPath().pathWithinApplication().value())
                    .queryParams(request.getQueryParams())
                    .build()
                    .toUriString();
            })
            .doOnNext(uri -> {
                if (log.isDebugEnabled()) {
                    log.debug("Proxy {} to {}", uri, proxyProperties.getEndpoint());
                }
            })
            .flatMap(uri -> webClient.get()
                .uri(uri)
                .headers(httpHeaders -> httpHeaders.addAll(exchange.getRequest().getHeaders()))
                .exchangeToMono(clientResponse -> {
                    var response = exchange.getResponse();
                    // set headers
                    response.getHeaders().putAll(clientResponse.headers().asHttpHeaders());
                    // set cookies
                    response.getCookies().putAll(clientResponse.cookies());
                    // set status code
                    response.setStatusCode(clientResponse.statusCode());
                    var body = clientResponse.body(BodyExtractors.toDataBuffers());
                    return exchange.getResponse().writeAndFlushWith(Mono.just(body));
                }));
    }
}
