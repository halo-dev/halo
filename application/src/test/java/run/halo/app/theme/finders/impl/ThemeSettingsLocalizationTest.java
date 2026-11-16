package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.localization.LocalizationContext;
import run.halo.app.localization.LocalizationScope;
import run.halo.app.localization.ResourceLocalizationService;

class ThemeSettingsLocalizationTest {
    @Test
    void localizesThemeConfigOnlyWhenARequestContextExists() {
        var client = mock(ReactiveExtensionClient.class);
        var localization = mock(ResourceLocalizationService.class);
        var finder = new ThemeFinderImpl(client, mock(SystemConfigFetcher.class), localization);
        var theme = new Theme();
        var metadata = new Metadata();
        metadata.setName("theme-test");
        theme.setMetadata(metadata);
        var spec = new Theme.ThemeSpec();
        spec.setConfigMapName("theme-config");
        theme.setSpec(spec);
        when(client.fetch(Theme.class, "theme-test")).thenReturn(Mono.just(theme));
        when(client.fetch(ConfigMap.class, "theme-config")).thenReturn(Mono.just(config("源文")));
        when(localization.localizeSettings(any(), any())).thenReturn(Mono.just(config("Welcome")));

        assertThat(finder.getByName("theme-test")
                        .block()
                        .getConfig()
                        .at("/basic/notice")
                        .asText())
                .isEqualTo("源文");
        verifyNoInteractions(localization);

        var context = new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);
        assertThat(finder.getByName("theme-test")
                        .contextWrite(reactor -> reactor.put(LocalizationContext.class, context))
                        .block()
                        .getConfig()
                        .at("/basic/notice")
                        .asText())
                .isEqualTo("Welcome");
        assertThat(finder.getByName("theme-test")
                        .block()
                        .getConfig()
                        .at("/basic/notice")
                        .asText())
                .isEqualTo("源文");
    }

    private static ConfigMap config(String notice) {
        var value = new ConfigMap();
        value.setData(Map.of("basic", "{\"notice\":\"" + notice + "\"}"));
        return value;
    }
}
