package run.halo.app.infra.actuator;

import java.time.Duration;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.stereotype.Component;

/**
 * Global info endpoint.
 */
@WebEndpoint(id = "globalinfo")
@Component
public class GlobalInfoEndpoint {

    private final GlobalInfoService globalInfoService;

    public GlobalInfoEndpoint(GlobalInfoService globalInfoService) {
        this.globalInfoService = globalInfoService;
    }

    @ReadOperation
    public GlobalInfo globalInfo() {
        return globalInfoService.getGlobalInfo().block(Duration.ofMinutes(1));
    }

}
