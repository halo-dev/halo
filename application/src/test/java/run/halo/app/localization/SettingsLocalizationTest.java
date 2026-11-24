package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

class SettingsLocalizationTest {
    private final ExtensionGetter getter = mock(ExtensionGetter.class);
    private final ResourceLocalizationProvider provider = mock(ResourceLocalizationProvider.class);
    private final DefaultResourceLocalizationService service = new DefaultResourceLocalizationService(
            getter, JsonMapper.builder().findAndAddModules().build());

    @BeforeEach
    void setup() {
        when(getter.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.just(provider));
    }

    @Test
    void isolatesInputAndOutputAndPreservesTheOriginalIdentity() {
        var source = source();
        when(provider.localizeSettings(any(), any())).thenAnswer(invocation -> {
            ConfigMap copy = invocation.getArgument(0);
            copy.getMetadata().setName("unexpected-name");
            copy.getData().put("notice", "Welcome");
            return Mono.just(copy);
        });

        var projected = service.localizeSettings(source, context(LocalizationScope.PUBLIC_PAGE))
                .block();

        assertThat(projected).isNotNull().isNotSameAs(source);
        assertThat(projected.getMetadata().getName()).isEqualTo("theme-settings");
        assertThat(projected.getData()).containsEntry("notice", "Welcome").containsEntry("color", "blue");
        projected.getData().put("color", "red");
        assertThat(source.getMetadata().getName()).isEqualTo("theme-settings");
        assertThat(source.getData()).containsEntry("notice", "欢迎").containsEntry("color", "blue");
    }

    @Test
    void onlyPublicRequestsCanProjectSettings() {
        var source = source();
        for (var scope :
                List.of(LocalizationScope.CONSOLE, LocalizationScope.INTERNAL, LocalizationScope.SEARCH_INDEX)) {
            assertThat(service.localizeSettings(source, context(scope)).block()).isSameAs(source);
        }
        var sourceLanguage =
                new LocalizationContext(Locale.CHINA, Locale.CHINA, List.of(), LocalizationScope.PUBLIC_PAGE);
        assertThat(service.localizeSettings(source, sourceLanguage).block()).isSameAs(source);
        verifyNoInteractions(getter, provider);
    }

    @Test
    void publicApiUsesTheSameProjection() {
        var source = source();
        when(provider.localizeSettings(any(), any())).thenAnswer(invocation -> {
            ConfigMap copy = invocation.getArgument(0);
            copy.putDataItem("notice", "Welcome");
            return Mono.just(copy);
        });
        assertThat(service.localizeSettings(source, context(LocalizationScope.PUBLIC_API))
                        .block()
                        .getData())
                .containsEntry("notice", "Welcome");
    }

    @Test
    void providerConfigurationReadsDoNotRecursivelyLocalize() {
        var source = source();
        var context = context(LocalizationScope.PUBLIC_PAGE);
        when(provider.localizeSettings(any(), any())).thenAnswer(invocation -> {
            assertThat(invocation.<LocalizationContext>getArgument(1)).isEqualTo(context);
            return LocalizationContext.current().hasElement().map(present -> {
                assertThat(present).isFalse();
                return invocation.<ConfigMap>getArgument(0);
            });
        });

        assertThat(service.localizeSettings(source, context)
                        .contextWrite(reactor -> reactor.put(LocalizationContext.class, context))
                        .block())
                .isSameAs(source);
    }

    @Test
    void missingProviderAndEmptyResultsKeepTheSource() {
        var source = source();
        when(getter.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.empty());
        assertThat(service.localizeSettings(source, context(LocalizationScope.PUBLIC_PAGE))
                        .block())
                .isSameAs(source);

        when(getter.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.just(provider));
        when(provider.localizeSettings(any(), any())).thenReturn(Mono.empty());
        assertThat(service.localizeSettings(source, context(LocalizationScope.PUBLIC_PAGE))
                        .block())
                .isSameAs(source);
    }

    @Test
    void providerFailureCannotChangeTheOriginalOrBreakThePage() {
        var source = source();
        when(provider.localizeSettings(any(), any())).thenAnswer(invocation -> {
            ConfigMap copy = invocation.getArgument(0);
            copy.getData().clear();
            throw new IllegalStateException("Unavailable");
        });
        assertThat(service.localizeSettings(source, context(LocalizationScope.PUBLIC_PAGE))
                        .block())
                .isSameAs(source);
        assertThat(source.getData()).containsEntry("notice", "欢迎");
    }

    @Test
    void slowProviderHasABoundedFallback() {
        var source = source();
        when(provider.localizeSettings(any(), any())).thenReturn(Mono.never());
        StepVerifier.withVirtualTime(() -> service.localizeSettings(source, context(LocalizationScope.PUBLIC_PAGE)))
                .thenAwait(Duration.ofSeconds(3))
                .expectNext(source)
                .verifyComplete();
    }

    private static LocalizationContext context(LocalizationScope scope) {
        return new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), scope);
    }

    private static ConfigMap source() {
        var source = new ConfigMap();
        var metadata = new Metadata();
        metadata.setName("theme-settings");
        source.setMetadata(metadata);
        source.setData(new LinkedHashMap<>(Map.of("notice", "欢迎", "color", "blue")));
        return source;
    }
}
