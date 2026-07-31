package run.halo.app.security.preauth;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

/**
 * Fetches the agreement pages configured for registration.
 *
 * @author johnniang
 * @since 2.26.0
 */
@Component
public class AgreementPageFetcher {

    private final SystemConfigFetcher systemConfigFetcher;

    private final ReactiveExtensionClient client;

    public AgreementPageFetcher(SystemConfigFetcher systemConfigFetcher, ReactiveExtensionClient client) {
        this.systemConfigFetcher = systemConfigFetcher;
        this.client = client;
    }

    public Mono<List<HashMap<String, String>>> fetchAgreementPages() {
        return Optional.ofNullable(systemConfigFetcher)
                .map(fetcher -> fetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                        .flatMapMany(userSetting -> {
                            var pages = userSetting.getRequiredAgreementPages();
                            if (CollectionUtils.isEmpty(pages)) {
                                return Flux.empty();
                            }
                            return Flux.fromIterable(pages);
                        })
                        .flatMap(pageName -> client.fetch(SinglePage.class, pageName)
                                .map(page -> {
                                    var map = new HashMap<String, String>();
                                    map.put("title", page.getSpec().getTitle());
                                    var status = page.getStatus();
                                    if (status != null) {
                                        map.put("permalink", status.getPermalink());
                                    }
                                    return map;
                                })
                                .onErrorResume(e -> Mono.empty()))
                        .collectList())
                .orElseGet(() -> Mono.just(List.of()));
    }
}
