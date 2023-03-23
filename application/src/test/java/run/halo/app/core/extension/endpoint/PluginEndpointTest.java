package run.halo.app.core.extension.endpoint;

import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

import com.github.zafarkhaja.semver.Version;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.service.PluginService;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.plugin.PluginProperties;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PluginEndpointTest {

    @Mock
    PluginProperties pluginProperties;

    @Mock
    private ReactiveExtensionClient client;

    @Mock
    SystemVersionSupplier systemVersionSupplier;

    @Mock
    PluginService pluginService;

    @InjectMocks
    PluginEndpoint endpoint;

    @Nested
    class PluginListTest {

        @Test
        void shouldListEmptyPluginsWhenNoPlugins() {
            when(client.list(same(Plugin.class), any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(ListResult.emptyResult()));

            bindToRouterFunction(endpoint.endpoint())
                .build()
                .get().uri("/plugins")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(0)
                .jsonPath("$.total").isEqualTo(0);
        }

        @Test
        void shouldListPluginsWhenPluginPresent() {
            var plugins = List.of(
                createPlugin("fake-plugin-1"),
                createPlugin("fake-plugin-2"),
                createPlugin("fake-plugin-3")
            );
            var expectResult = new ListResult<>(plugins);
            when(client.list(same(Plugin.class), any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(expectResult));

            bindToRouterFunction(endpoint.endpoint())
                .build()
                .get().uri("/plugins")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(3)
                .jsonPath("$.total").isEqualTo(3);
        }

        @Test
        void shouldFilterPluginsWhenKeywordProvided() {
            var expectPlugin =
                createPlugin("fake-plugin-2", "expected display name", "", false);
            var unexpectedPlugin1 =
                createPlugin("fake-plugin-1", "first fake display name", "", false);
            var unexpectedPlugin2 =
                createPlugin("fake-plugin-3", "second fake display name", "", false);
            var plugins = List.of(
                expectPlugin
            );
            var expectResult = new ListResult<>(plugins);
            when(client.list(same(Plugin.class), any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(expectResult));

            bindToRouterFunction(endpoint.endpoint())
                .build()
                .get().uri("/plugins?keyword=Expected")
                .exchange()
                .expectStatus().isOk();

            verify(client).list(same(Plugin.class), argThat(
                    predicate -> predicate.test(expectPlugin)
                                 && !predicate.test(unexpectedPlugin1)
                                 && !predicate.test(unexpectedPlugin2)),
                any(), anyInt(), anyInt());
        }

        @Test
        void shouldFilterPluginsWhenEnabledProvided() {
            var expectPlugin =
                createPlugin("fake-plugin-2", "expected display name", "", true);
            var unexpectedPlugin1 =
                createPlugin("fake-plugin-1", "first fake display name", "", false);
            var unexpectedPlugin2 =
                createPlugin("fake-plugin-3", "second fake display name", "", false);
            var plugins = List.of(
                expectPlugin
            );
            var expectResult = new ListResult<>(plugins);
            when(client.list(same(Plugin.class), any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(expectResult));

            bindToRouterFunction(endpoint.endpoint())
                .build()
                .get().uri("/plugins?enabled=true")
                .exchange()
                .expectStatus().isOk();

            verify(client).list(same(Plugin.class), argThat(
                    predicate -> predicate.test(expectPlugin)
                                 && !predicate.test(unexpectedPlugin1)
                                 && !predicate.test(unexpectedPlugin2)),
                any(), anyInt(), anyInt());
        }

        @Test
        void shouldSortPluginsWhenCreationTimestampSet() {
            var expectPlugin =
                createPlugin("fake-plugin-2", "expected display name", "", true);
            var expectResult = new ListResult<>(List.of(expectPlugin));
            when(client.list(same(Plugin.class), any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(expectResult));

            bindToRouterFunction(endpoint.endpoint())
                .build()
                .get().uri("/plugins?sort=creationTimestamp,desc")
                .exchange()
                .expectStatus().isOk();

            verify(client).list(same(Plugin.class), any(), argThat(comparator -> {
                var now = Instant.now();
                var plugins = new ArrayList<>(List.of(
                    createPlugin("fake-plugin-a", now),
                    createPlugin("fake-plugin-b", now.plusSeconds(1)),
                    createPlugin("fake-plugin-c", now.plusSeconds(2))
                ));
                plugins.sort(comparator);
                return Objects.deepEquals(plugins, List.of(
                    createPlugin("fake-plugin-c", now.plusSeconds(2)),
                    createPlugin("fake-plugin-b", now.plusSeconds(1)),
                    createPlugin("fake-plugin-a", now)
                ));
            }), anyInt(), anyInt());
        }
    }

    @Nested
    class PluginUpgradeTest {

        WebTestClient webClient;

        Path tempDirectory;

        Path plugin002;

        @BeforeEach
        void setUp() throws URISyntaxException, IOException {
            webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
                .build();

            lenient().when(systemVersionSupplier.get()).thenReturn(Version.valueOf("0.0.0"));
            tempDirectory = Files.createTempDirectory("halo-test-plugin-upgrade-");
            lenient().when(pluginProperties.getPluginsRoot())
                .thenReturn(tempDirectory.resolve("plugins").toString());
            plugin002 = tempDirectory.resolve("plugin-0.0.2.jar");

            var plugin002Uri = requireNonNull(
                getClass().getClassLoader().getResource("plugin/plugin-0.0.2")).toURI();

            FileUtils.jar(Paths.get(plugin002Uri), tempDirectory.resolve("plugin-0.0.2.jar"));
        }

        @AfterEach
        void cleanUp() {
            FileUtils.deleteRecursivelyAndSilently(tempDirectory);
        }

        @Test
        void shouldResponseBadRequestIfNoPluginInstalledBefore() {
            var bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("file", new FileSystemResource(plugin002))
                .contentType(MediaType.MULTIPART_FORM_DATA);

            when(pluginService.upgrade(eq("fake-plugin"), isA(Path.class)))
                .thenReturn(Mono.error(new ServerWebInputException("plugin not found")));

            webClient.post().uri("/plugins/fake-plugin/upgrade")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus().isBadRequest();

            verify(pluginService).upgrade(eq("fake-plugin"), isA(Path.class));
        }

    }

    @Nested
    class UpdatePluginConfigTest {
        WebTestClient webClient;

        @BeforeEach
        void setUp() {
            webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
                .build();
        }

        @Test
        void updateWhenConfigMapNameIsNull() {
            Plugin plugin = createPlugin("fake-plugin");
            plugin.getSpec().setConfigMapName(null);

            when(client.fetch(eq(Plugin.class), eq("fake-plugin"))).thenReturn(Mono.just(plugin));
            webClient.put()
                .uri("/plugins/fake-plugin/config")
                .exchange()
                .expectStatus().isBadRequest();
        }

        @Test
        void updateWhenConfigMapNameNotMatch() {
            Plugin plugin = createPlugin("fake-plugin");
            plugin.getSpec().setConfigMapName("fake-config-map");

            when(client.fetch(eq(Plugin.class), eq("fake-plugin"))).thenReturn(Mono.just(plugin));
            webClient.put()
                .uri("/plugins/fake-plugin/config")
                .body(Mono.fromSupplier(() -> {
                    ConfigMap configMap = new ConfigMap();
                    configMap.setMetadata(new Metadata());
                    configMap.getMetadata().setName("not-match");
                    return configMap;
                }), ConfigMap.class)
                .exchange()
                .expectStatus().isBadRequest();
        }

        @Test
        void updateWhenConfigMapNameMatch() {
            Plugin plugin = createPlugin("fake-plugin");
            plugin.getSpec().setConfigMapName("fake-config-map");

            when(client.fetch(eq(Plugin.class), eq("fake-plugin"))).thenReturn(Mono.just(plugin));
            when(client.fetch(eq(ConfigMap.class), eq("fake-config-map"))).thenReturn(Mono.empty());
            when(client.create(any(ConfigMap.class))).thenReturn(Mono.empty());

            webClient.put()
                .uri("/plugins/fake-plugin/config")
                .body(Mono.fromSupplier(() -> {
                    ConfigMap configMap = new ConfigMap();
                    configMap.setMetadata(new Metadata());
                    configMap.getMetadata().setName("fake-config-map");
                    return configMap;
                }), ConfigMap.class)
                .exchange()
                .expectStatus().isOk();
        }
    }

    @Nested
    class PluginConfigAndSettingFetchTest {
        WebTestClient webClient;

        @BeforeEach
        void setUp() {
            webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
                .build();
        }

        @Test
        void fetchSetting() {
            Plugin plugin = createPlugin("fake");
            plugin.getSpec().setSettingName("fake-setting");

            when(client.fetch(eq(Setting.class), eq("fake-setting")))
                .thenReturn(Mono.just(new Setting()));

            when(client.fetch(eq(Plugin.class), eq("fake"))).thenReturn(Mono.just(plugin));
            webClient.get()
                .uri("/plugins/fake/setting")
                .exchange()
                .expectStatus().isOk();

            verify(client).fetch(eq(Setting.class), eq("fake-setting"));
            verify(client).fetch(eq(Plugin.class), eq("fake"));
        }

        @Test
        void fetchConfig() {
            Plugin plugin = createPlugin("fake");
            plugin.getSpec().setConfigMapName("fake-config");

            when(client.fetch(eq(ConfigMap.class), eq("fake-config")))
                .thenReturn(Mono.just(new ConfigMap()));

            when(client.fetch(eq(Plugin.class), eq("fake"))).thenReturn(Mono.just(plugin));
            webClient.get()
                .uri("/plugins/fake/config")
                .exchange()
                .expectStatus().isOk();

            verify(client).fetch(eq(ConfigMap.class), eq("fake-config"));
            verify(client).fetch(eq(Plugin.class), eq("fake"));
        }
    }

    Plugin createPlugin(String name) {
        return createPlugin(name, "fake display name", "fake description", null);
    }

    Plugin createPlugin(String name, String displayName, String description, Boolean enabled) {
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setCreationTimestamp(Instant.now());
        var spec = new Plugin.PluginSpec();
        spec.setDisplayName(displayName);
        spec.setDescription(description);
        spec.setEnabled(enabled);
        var plugin = new Plugin();
        plugin.setMetadata(metadata);
        plugin.setSpec(spec);
        return plugin;
    }

    Plugin createPlugin(String name, Instant creationTimestamp) {
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setCreationTimestamp(creationTimestamp);
        var spec = new Plugin.PluginSpec();
        var plugin = new Plugin();
        plugin.setMetadata(metadata);
        plugin.setSpec(spec);
        return plugin;
    }
}
