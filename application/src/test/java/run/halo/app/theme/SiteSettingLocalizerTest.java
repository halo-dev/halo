package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.SystemSetting;
import run.halo.app.localization.LocalizationContext;
import run.halo.app.localization.LocalizationScope;
import run.halo.app.localization.ResourceLocalizationService;
import run.halo.app.localization.ResourceReference;
import run.halo.app.theme.finders.vo.SiteSettingVo;

@ExtendWith(MockitoExtension.class)
class SiteSettingLocalizerTest {

    @Mock
    private ResourceLocalizationService localizationService;

    @InjectMocks
    private SiteSettingLocalizer siteSettingLocalizer;

    @Test
    void returnsSourceWithoutLocalizationContext() {
        var source = site();

        siteSettingLocalizer
                .localize(source)
                .as(StepVerifier::create)
                .expectNext(source)
                .verifyComplete();

        verify(localizationService, never()).localizeFields(any(), any(), any());
    }

    @Test
    void localizesPresentationFieldsAndPreservesOtherSettings() {
        var source = site();
        var context = new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);
        var reference =
                new ResourceReference(GroupVersionKind.fromExtension(ConfigMap.class), SystemSetting.SYSTEM_CONFIG);
        when(localizationService.localizeFields(eq(reference), any(), eq(context)))
                .thenAnswer(invocation -> {
                    Map<String, String> fields = invocation.getArgument(1);
                    assertThat(fields)
                            .containsEntry("title", "原站点")
                            .containsEntry("subtitle", "原副标题")
                            .containsEntry("seo.description", "原描述")
                            .containsEntry("seo.keywords", "原关键词");
                    return Mono.just(Map.of(
                            "title",
                            "Site",
                            "subtitle",
                            "Subtitle",
                            "seo.description",
                            "Description",
                            "seo.keywords",
                            "Keywords"));
                });

        siteSettingLocalizer
                .localize(source)
                .contextWrite(ctx -> ctx.put(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(localized -> {
                    assertThat(localized.title()).isEqualTo("Site");
                    assertThat(localized.subtitle()).isEqualTo("Subtitle");
                    assertThat(localized.language()).isEqualTo("en");
                    assertThat(localized.seo().description()).isEqualTo("Description");
                    assertThat(localized.seo().keywords()).isEqualTo("Keywords");
                    assertThat(localized.seo().blockSpiders()).isTrue();
                    assertThat(localized.logo()).isEqualTo(source.logo());
                    assertThat(localized.routes()).isEqualTo(source.routes());
                })
                .verifyComplete();

        verify(localizationService).localizeFields(eq(reference), any(), eq(context));
    }

    private static SiteSettingVo site() {
        return SiteSettingVo.builder()
                .title("原站点")
                .subtitle("原副标题")
                .logo("/logo.png")
                .language("zh-CN")
                .seo(new SiteSettingVo.SeoSetting(true, "原关键词", "原描述"))
                .routes(new SiteSettingVo.Routes("/categories", "/tags", "/archives"))
                .build();
    }
}
