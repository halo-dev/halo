package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.localization.LocalizationContext;
import run.halo.app.localization.LocalizationScope;
import run.halo.app.localization.ResourceLocalizationService;

class SettingFetcherLocalizationTest {
    @Test
    void cachesOriginalConfigurationButProjectsEachRequestIndependently() {
        var client = mock(ReactiveExtensionClient.class);
        var localization = mock(ResourceLocalizationService.class);
        var source = config("源文");
        when(client.fetch(ConfigMap.class, "plugin-config")).thenReturn(Mono.just(source));
        when(localization.localizeSettings(any(), any())).thenAnswer(invocation -> {
            LocalizationContext context = invocation.getArgument(1);
            return Mono.just(config(context.targetLocale().toLanguageTag()));
        });
        var plugin = PluginContext.builder()
                .name("sample")
                .configMapName("plugin-config")
                .build();
        var fetcher = new DefaultReactiveSettingFetcher(plugin, client, localization);

        var results = Flux.merge(
                        fetcher.getSettingValue("basic")
                                .contextWrite(
                                        context -> context.put(LocalizationContext.class, language(Locale.ENGLISH))),
                        fetcher.getSettingValue("basic")
                                .contextWrite(
                                        context -> context.put(LocalizationContext.class, language(Locale.JAPANESE))))
                .map(value -> value.get("notice").asText())
                .collectList()
                .block();

        assertThat(results).containsExactlyInAnyOrder("en", "ja");
        assertThat(fetcher.getSettingValue("basic").block().get("notice").asText())
                .isEqualTo("源文");
        assertThat(fetcher.get("basic").block().get("notice").asText()).isEqualTo("源文");
        verify(client, times(1)).fetch(ConfigMap.class, "plugin-config");
        verify(localization).localizeSettings(source, language(Locale.ENGLISH));
        verify(localization).localizeSettings(source, language(Locale.JAPANESE));
    }

    @Test
    void legacyJacksonReaderAlsoUsesThePublicProjection() {
        var client = mock(ReactiveExtensionClient.class);
        var localization = mock(ResourceLocalizationService.class);
        when(client.fetch(ConfigMap.class, "plugin-config")).thenReturn(Mono.just(config("源文")));
        when(localization.localizeSettings(any(), any())).thenReturn(Mono.just(config("Welcome")));
        var plugin = PluginContext.builder()
                .name("sample")
                .configMapName("plugin-config")
                .build();
        var fetcher = new DefaultReactiveSettingFetcher(plugin, client, localization);

        assertThat(fetcher.get("basic")
                        .contextWrite(context -> context.put(LocalizationContext.class, language(Locale.ENGLISH)))
                        .block()
                        .get("notice")
                        .asText())
                .isEqualTo("Welcome");
    }

    private static LocalizationContext language(Locale target) {
        return new LocalizationContext(Locale.CHINA, target, List.of(), LocalizationScope.PUBLIC_PAGE);
    }

    private static ConfigMap config(String notice) {
        var value = new ConfigMap();
        var metadata = new Metadata();
        metadata.setName("plugin-config");
        value.setMetadata(metadata);
        value.setData(Map.of("basic", "{\"notice\":\"" + notice + "\"}"));
        return value;
    }
}
