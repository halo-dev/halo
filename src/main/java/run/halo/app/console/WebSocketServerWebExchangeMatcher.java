package run.halo.app.console;

import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class WebSocketServerWebExchangeMatcher implements ServerWebExchangeMatcher {
    @Override
    public Mono<MatchResult> matches(ServerWebExchange exchange) {
        var headers = exchange.getRequest().getHeaders();
        if (!headers.getConnection().contains("Upgrade")) {
            return MatchResult.notMatch();
        }
        var upgrade = headers.getUpgrade();
        if (!"websocket".equalsIgnoreCase(upgrade)) {
            return MatchResult.notMatch();
        }
        return MatchResult.match();
    }
}
