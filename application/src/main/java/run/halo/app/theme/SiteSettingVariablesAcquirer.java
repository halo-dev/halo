package run.halo.app.theme;

import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.theme.finders.vo.SiteSettingVo;

/**
 * Site setting variables acquirer.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class SiteSettingVariablesAcquirer implements ViewContextBasedVariablesAcquirer {

    private final SystemConfigFetcher environmentFetcher;
    private final ExternalUrlSupplier externalUrlSupplier;
    private final SystemVersionSupplier systemVersionSupplier;
    private final SiteSettingLocalizer siteSettingLocalizer;

    @Override
    public Mono<Map<String, Object>> acquire(ServerWebExchange exchange) {
        return environmentFetcher
                .getConfig()
                .flatMap(config -> {
                    var site = SiteSettingVo.from(config)
                            .withUrl(externalUrlSupplier.getURL(exchange.getRequest()))
                            .withVersion(systemVersionSupplier.get().toString());
                    return siteSettingLocalizer.localize(site);
                })
                .map(site -> Map.<String, Object>of("site", site));
    }
}
