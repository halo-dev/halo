package run.halo.app.infra.actuator;

import reactor.core.publisher.Mono;

/**
 * Global info service.
 *
 * @author johnniang
 * @since 2.20.0
 */
public interface GlobalInfoService {

    /**
     * Get global info.
     *
     * @return global info
     */
    Mono<GlobalInfo> getGlobalInfo();

}
