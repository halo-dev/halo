package run.halo.app.security.preauth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

/** Fetches the titles and permalinks of the required agreement pages. */
@Component
@RequiredArgsConstructor
class AgreementPageFetcher {

    private final SystemConfigFetcher systemConfigFetcher;
    private final ReactiveExtensionClient extensionClient;

    Mono<List<Map<String, String>>> fetchAgreementPages() {
        return systemConfigFetcher
                .fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                .flatMapMany(userSetting -> {
                    var pages = userSetting.getRequiredAgreementPages();
                    if (CollectionUtils.isEmpty(pages)) {
                        return Flux.empty();
                    }
                    return Flux.fromIterable(pages);
                })
                .flatMap(pageName -> extensionClient
                        .fetch(SinglePage.class, pageName)
                        .map(page -> {
                            Map<String, String> map = new HashMap<>();
                            map.put("title", page.getSpec().getTitle());
                            var status = page.getStatus();
                            if (status != null) {
                                map.put("permalink", status.getPermalink());
                            }
                            return map;
                        })
                        .onErrorResume(e -> Mono.empty()))
                .collectList();
    }
}
