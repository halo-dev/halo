package run.halo.app.security.preauth;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.infra.utils.HaloUtils;

/**
 * Pre-auth two-factor endpoints.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Component
class PreAuthTwoFactorEndpoint {

    @Bean
    RouterFunction<ServerResponse> preAuthTwoFactorEndpoints(GlobalInfoService globalInfoService) {
        return RouterFunctions.route()
            .GET("/challenges/two-factor/totp",
                request -> ServerResponse.ok().render("challenges/two-factor/totp", Map.of(
                    "globalInfo", globalInfoService.getGlobalInfo()
                ))
            )
            .before(HaloUtils.noCache())
            .build();
    }

}
