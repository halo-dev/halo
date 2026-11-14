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

@Component
@RequiredArgsConstructor
class AgreementPageFetcher {

    private final SystemConfigFetcher systemConfigFetcher;

    private final ReactiveExtensionClient extensionClient;

    Mono<List<Map<String, String>>> fetchAgreementPages() {
        return systemConfigFetcher
                .fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                .flatMapMany(setting -> CollectionUtils.isEmpty(setting.getRequiredAgreementPages())
                        ? Flux.empty()
                        : Flux.fromIterable(setting.getRequiredAgreementPages()))
                .concatMap(name -> extensionClient
                        .fetch(SinglePage.class, name)
                        .switchIfEmpty(
                                Mono.error(new IllegalStateException("Required agreement page not found: " + name)))
                        .map(page -> {
                            Map<String, String> result = new HashMap<>();
                            result.put("title", page.getSpec().getTitle());
                            if (page.getStatus() != null) {
                                result.put("permalink", page.getStatus().getPermalink());
                            }
                            return result;
                        }))
                .collectList();
    }
}
