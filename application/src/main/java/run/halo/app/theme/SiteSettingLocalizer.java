package run.halo.app.theme;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.SystemSetting;
import run.halo.app.localization.LocalizationContext;
import run.halo.app.localization.ResourceLocalizationService;
import run.halo.app.localization.ResourceReference;
import run.halo.app.localization.SiteLocalizationDescriptor;
import run.halo.app.theme.finders.vo.SiteSettingVo;

@Component
@AllArgsConstructor
class SiteSettingLocalizer {

    private static final ResourceReference SYSTEM_CONFIG =
            new ResourceReference(GroupVersionKind.fromExtension(ConfigMap.class), SystemSetting.SYSTEM_CONFIG);

    private final ResourceLocalizationService localizationService;

    Mono<SiteSettingVo> localize(SiteSettingVo site) {
        var fields = new LinkedHashMap<String, String>();
        fields.put(SiteLocalizationDescriptor.TITLE, site.title());
        fields.put(SiteLocalizationDescriptor.SUBTITLE, site.subtitle());
        fields.put(SiteLocalizationDescriptor.SEO_DESCRIPTION, site.seo().description());
        fields.put(SiteLocalizationDescriptor.SEO_KEYWORDS, site.seo().keywords());
        return LocalizationContext.current()
                .flatMap(context -> localizationService
                        .localizeFields(SYSTEM_CONFIG, fields, context)
                        .map(values -> apply(site, values, context)))
                .defaultIfEmpty(site);
    }

    private static SiteSettingVo apply(SiteSettingVo site, Map<String, String> values, LocalizationContext context) {
        return site.withTitle(values.get(SiteLocalizationDescriptor.TITLE))
                .withSubtitle(values.get(SiteLocalizationDescriptor.SUBTITLE))
                .withLanguage(context.targetLocale().toLanguageTag())
                .withSeo(new SiteSettingVo.SeoSetting(
                        site.seo().blockSpiders(),
                        values.get(SiteLocalizationDescriptor.SEO_KEYWORDS),
                        values.get(SiteLocalizationDescriptor.SEO_DESCRIPTION)));
    }
}
