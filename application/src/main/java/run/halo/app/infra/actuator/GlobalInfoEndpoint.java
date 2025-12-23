package run.halo.app.infra.actuator;

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Global info endpoint.
 */
@WebEndpoint(id = "globalinfo")
@Component
class GlobalInfoEndpoint {

    private final GlobalInfoService globalInfoService;

    public GlobalInfoEndpoint(GlobalInfoService globalInfoService) {
        this.globalInfoService = globalInfoService;
    }

    @ReadOperation
    public Mono<GlobalInfo> globalInfo() {
        return globalInfoService.getGlobalInfo();
    }

}
