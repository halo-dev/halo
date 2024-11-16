package run.halo.app.infra.console;

import static run.halo.app.infra.console.WebSocketUtils.isWebSocketUpgrade;

import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.ServerRequest;

public class WebSocketRequestPredicate implements RequestPredicate {

    @Override
    public boolean test(ServerRequest request) {
        var httpHeaders = request.exchange().getRequest().getHeaders();
        return isWebSocketUpgrade(httpHeaders);
    }
}
