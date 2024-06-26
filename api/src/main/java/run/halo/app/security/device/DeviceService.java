package run.halo.app.security.device;

import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface DeviceService {

    Mono<Void> loginSuccess(ServerWebExchange exchange, Authentication successfullAuthentication);

    Mono<Void> changeSessionId(ServerWebExchange exchange);

    Mono<Void> revoke(String principalName, String deviceId);
}
