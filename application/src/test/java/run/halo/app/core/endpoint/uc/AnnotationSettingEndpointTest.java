package run.halo.app.core.endpoint.uc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.AnnotationSetting;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.PluginService;
import run.halo.app.theme.service.ThemeService;

@ExtendWith(MockitoExtension.class)
class AnnotationSettingEndpointTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    PluginService pluginService;

    @Mock
    ThemeService themeService;

    @InjectMocks
    AnnotationSettingEndpoint endpoint;

    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
            .build();
    }

    @Test
    void shouldFetchAnnotationSettings() {
        when(themeService.fetchActivatedThemeName()).thenReturn(Mono.just("fake-theme"));
        when(pluginService.getStartedPluginNames()).thenReturn(Flux.just("plugin-1", "plugin-2"));

        var annotationSetting = new AnnotationSetting();
        annotationSetting.setMetadata(new Metadata());
        annotationSetting.getMetadata().setName("fake-annotation");
        when(
            client.listAll(same(AnnotationSetting.class), any(ListOptions.class), any(Sort.class))
        ).thenReturn(Flux.just(annotationSetting));

        webTestClient.get()
            .uri("/annotationsettings?targetRef={targetRef}", "content.halo.run/Post")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(AnnotationSetting.class)
            .isEqualTo(List.of(annotationSetting));

        verify(client).listAll(
            same(AnnotationSetting.class),
            assertArg(listOptions -> {
                var condition = listOptions.toCondition();
                assertEquals("""
                    (\
                    (metadata.labels['theme.halo.run/theme-name'] = 'fake-theme' \
                    OR metadata.labels['plugin.halo.run/plugin-name'] IN ('plugin-1', 'plugin-2')\
                    ) \
                    AND spec.targetRef = content.halo.run/Post\
                    )""", condition.toString());
            }),
            any(Sort.class));
    }

    @Test
    void shouldFetchAnnotationSettingsWithoutActivatedTheme() {
        when(themeService.fetchActivatedThemeName()).thenReturn(Mono.empty());
        when(pluginService.getStartedPluginNames()).thenReturn(Flux.just("plugin-1", "plugin-2"));

        var annotationSetting = new AnnotationSetting();
        annotationSetting.setMetadata(new Metadata());
        annotationSetting.getMetadata().setName("fake-annotation");
        when(
            client.listAll(same(AnnotationSetting.class), any(ListOptions.class), any(Sort.class))
        ).thenReturn(Flux.just(annotationSetting));

        webTestClient.get()
            .uri("/annotationsettings?targetRef={targetRef}", "content.halo.run/Post")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(AnnotationSetting.class)
            .isEqualTo(List.of(annotationSetting));

        verify(client).listAll(
            same(AnnotationSetting.class),
            assertArg(listOptions -> {
                var condition = listOptions.toCondition();
                assertEquals("""
                    (\
                    metadata.labels['plugin.halo.run/plugin-name'] IN ('plugin-1', 'plugin-2') \
                    AND spec.targetRef = content.halo.run/Post\
                    )""", condition.toString());
            }),
            any(Sort.class));
    }

    @Test
    void shouldFetchAnnotationSettingsWithoutStartedPlugins() {
        when(themeService.fetchActivatedThemeName()).thenReturn(Mono.just("fake-theme"));
        when(pluginService.getStartedPluginNames()).thenReturn(Flux.empty());

        var annotationSetting = new AnnotationSetting();
        annotationSetting.setMetadata(new Metadata());
        annotationSetting.getMetadata().setName("fake-annotation");
        when(
            client.listAll(same(AnnotationSetting.class), any(ListOptions.class), any(Sort.class))
        ).thenReturn(Flux.just(annotationSetting));

        webTestClient.get()
            .uri("/annotationsettings?targetRef={targetRef}", "content.halo.run/Post")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(AnnotationSetting.class)
            .isEqualTo(List.of(annotationSetting));

        verify(client).listAll(
            same(AnnotationSetting.class),
            assertArg(listOptions -> {
                var condition = listOptions.toCondition();
                assertEquals("""
                    (\
                    metadata.labels['theme.halo.run/theme-name'] = 'fake-theme' \
                    AND spec.targetRef = content.halo.run/Post\
                    )""", condition.toString());
            }),
            any(Sort.class));
    }

    @Test
    void shouldFetchAnnotationSettingsWithoutActivatedThemeAndStartedPlugins() {
        when(themeService.fetchActivatedThemeName()).thenReturn(Mono.empty());
        when(pluginService.getStartedPluginNames()).thenReturn(Flux.empty());

        var annotationSetting = new AnnotationSetting();
        annotationSetting.setMetadata(new Metadata());
        annotationSetting.getMetadata().setName("fake-annotation");
        when(
            client.listAll(same(AnnotationSetting.class), any(ListOptions.class), any(Sort.class))
        ).thenReturn(Flux.just(annotationSetting));

        webTestClient.get()
            .uri("/annotationsettings?targetRef={targetRef}", "content.halo.run/Post")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(AnnotationSetting.class)
            .isEqualTo(List.of(annotationSetting));

        verify(client).listAll(
            same(AnnotationSetting.class),
            assertArg(listOptions -> {
                var condition = listOptions.toCondition();
                assertEquals("""
                    (\
                    EMPTY AND spec.targetRef = content.halo.run/Post\
                    )""", condition.toString());
            }),
            any(Sort.class));
    }
}