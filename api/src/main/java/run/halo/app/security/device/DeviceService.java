package run.halo.app.security.device;

import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Device;

public interface DeviceService {

    Mono<Void> loginSuccess(ServerWebExchange exchange, Authentication successfullAuthentication);

    Mono<Void> changeSessionId(ServerWebExchange exchange);

    Mono<Void> revoke(String principalName, String deviceId);

    Mono<Void> revoke(String username);

    /**
     * Resolve the current device from the request.
     *
     * @param exchange the current server web exchange
     * @return the current device, or empty if not found or session ID mismatch
     * @since 2.24.1
     */
    Mono<Device> resolveCurrentDevice(ServerWebExchange exchange);
}
