package run.halo.app.theme;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.localization.*;
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
    private final ResourceLocalizationService localizationService;

    @Override
    public Mono<Map<String, Object>> acquire(ServerWebExchange exchange) {
        return environmentFetcher.getConfig().flatMap(config -> {
            var site = SiteSettingVo.from(config)
                    .withUrl(externalUrlSupplier.getURL(exchange.getRequest()))
                    .withVersion(systemVersionSupplier.get().toString());
            var fields = new LinkedHashMap<String, String>();
            fields.put("title", site.title());
            fields.put("subtitle", site.subtitle());
            fields.put("seo.description", site.seo().description());
            fields.put("seo.keywords", site.seo().keywords());
            return LocalizationContext.current()
                    .flatMap(context -> localizationService
                            .localizeFields(
                                    new ResourceReference(GroupVersionKind.fromExtension(ConfigMap.class), "system"),
                                    fields,
                                    context)
                            .map(values -> site.withTitle(values.get("title"))
                                    .withSubtitle(values.get("subtitle"))
                                    .withLanguage(context.targetLocale().toLanguageTag())
                                    .withSeo(new SiteSettingVo.SeoSetting(
                                            site.seo().blockSpiders(),
                                            values.get("seo.keywords"),
                                            values.get("seo.description")))))
                    .defaultIfEmpty(site)
                    .map(projected -> Map.<String, Object>of("site", projected));
        });
    }
}
