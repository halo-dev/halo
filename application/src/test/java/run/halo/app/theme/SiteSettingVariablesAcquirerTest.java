package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.zafarkhaja.semver.Version;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.localization.LocalizationContext;
import run.halo.app.localization.LocalizationScope;
import run.halo.app.localization.ResourceLocalizationService;
import run.halo.app.theme.finders.vo.SiteSettingVo;

/**
 * Tests for {@link SiteSettingVariablesAcquirer}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
public class SiteSettingVariablesAcquirerTest {
    @Mock
    private ExternalUrlSupplier externalUrlSupplier;

    @Mock
    private SystemVersionSupplier systemVersionSupplier;

    @Mock
    private SystemConfigFetcher environmentFetcher;

    @Mock
    private ResourceLocalizationService localizationService;

    @InjectMocks
    private SiteSettingVariablesAcquirer siteSettingVariablesAcquirer;

    @Test
    void acquireWhenExternalUrlSet() throws MalformedURLException {
        var url = new URL("https://halo.run");
        when(externalUrlSupplier.getURL(any())).thenReturn(url);
        when(systemVersionSupplier.get()).thenReturn(Version.parse("0.0.0-alpha.1"));
        when(environmentFetcher.getConfig()).thenReturn(Mono.just(Map.of()));

        siteSettingVariablesAcquirer
                .acquire(mock(ServerWebExchange.class))
                .as(StepVerifier::create)
                .consumeNextWith(result -> {
                    assertThat(result).containsKey("site");
                    assertThat(result.get("site")).isInstanceOf(SiteSettingVo.class);
                    var site = (SiteSettingVo) result.get("site");
                    assertThat(site).extracting(SiteSettingVo::url).isEqualTo(url);
                    assertThat(site).extracting(SiteSettingVo::version).isEqualTo("0.0.0-alpha.1");
                })
                .verifyComplete();
        verify(externalUrlSupplier).getURL(any());
    }

    @Test
    void localizationFallbackPreservesUnsetSiteFields() throws MalformedURLException {
        var url = new URL("https://halo.run");
        when(externalUrlSupplier.getURL(any())).thenReturn(url);
        when(systemVersionSupplier.get()).thenReturn(Version.parse("0.0.0-alpha.1"));
        var config = Map.of("basic", "{\"title\":\"Site\"}");
        when(environmentFetcher.getConfig()).thenReturn(Mono.just(config));
        var context = new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);
        when(localizationService.localizeFields(any(), any(), eq(context)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(1)));

        siteSettingVariablesAcquirer
                .acquire(mock(ServerWebExchange.class))
                .contextWrite(ctx -> ctx.put(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(result -> {
                    var site = (SiteSettingVo) result.get("site");
                    assertThat(site.title()).isEqualTo("Site");
                    assertThat(site.subtitle()).isNull();
                    assertThat(site.seo().description()).isNull();
                    assertThat(site.seo().keywords()).isNull();
                    assertThat(site.language()).isEqualTo("en");
                })
                .verifyComplete();
    }

    @Test
    void localizesSitePresentationWithoutChangingOtherSettings() throws MalformedURLException {
        var url = new URL("https://halo.run");
        when(externalUrlSupplier.getURL(any())).thenReturn(url);
        when(systemVersionSupplier.get()).thenReturn(Version.parse("0.0.0-alpha.1"));
        var config = Map.of(
                "basic",
                "{\"title\":\"原站点\",\"subtitle\":\"原副标题\"}",
                "seo",
                "{\"description\":\"原描述\",\"keywords\":\"原关键词\",\"blockSpiders\":true}");
        when(environmentFetcher.getConfig()).thenReturn(Mono.just(config));
        var context = new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);
        when(localizationService.localizeFields(any(), any(), eq(context)))
                .thenReturn(Mono.just(Map.of(
                        "title",
                        "Site",
                        "subtitle",
                        "Subtitle",
                        "seo.description",
                        "Description",
                        "seo.keywords",
                        "Keywords")));
        siteSettingVariablesAcquirer
                .acquire(mock(ServerWebExchange.class))
                .contextWrite(ctx -> ctx.put(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(result -> {
                    var site = (SiteSettingVo) result.get("site");
                    assertThat(site.title()).isEqualTo("Site");
                    assertThat(site.subtitle()).isEqualTo("Subtitle");
                    assertThat(site.language()).isEqualTo("en");
                    assertThat(site.seo().description()).isEqualTo("Description");
                    assertThat(site.seo().keywords()).isEqualTo("Keywords");
                    assertThat(site.seo().blockSpiders()).isTrue();
                    assertThat(site.url()).isEqualTo(url);
                    assertThat(site.routes())
                            .isEqualTo(SiteSettingVo.from(config).routes());
                })
                .verifyComplete();
    }
}
