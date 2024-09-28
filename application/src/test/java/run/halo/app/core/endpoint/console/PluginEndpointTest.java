package run.halo.app.core.endpoint.console;

import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

import com.github.zafarkhaja.semver.Version;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.user.service.SettingConfigService;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.plugin.PluginService;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PluginEndpointTest {

    @Mock
    private ReactiveExtensionClient client;

    @Mock
    SystemVersionSupplier systemVersionSupplier;

    @Mock
    PluginService pluginService;

    @Mock
    SettingConfigService settingConfigService;

    @Spy
    WebProperties webProperties = new WebProperties();

    @InjectMocks
    PluginEndpoint endpoint;

    @Nested
    class PluginListTest {

        @Test
        void shouldListEmptyPluginsWhenNoPlugins() {
            when(client.listBy(same(Plugin.class), any(ListOptions.class), any(PageRequest.class)))
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
            when(client.listBy(same(Plugin.class), any(ListOptions.class), any(PageRequest.class)))
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
            when(client.listBy(same(Plugin.class), any(ListOptions.class), any(PageRequest.class)))
                .thenReturn(Mono.just(expectResult));

            bindToRouterFunction(endpoint.endpoint())
                .build()
                .get().uri("/plugins?keyword=Expected")
                .exchange()
                .expectStatus().isOk();
        }

        @Test
        void shouldFilterPluginsWhenEnabledProvided() {
            var expectPlugin =
                createPlugin("fake-plugin-2", "expected display name", "", true);
            var plugins = List.of(
                expectPlugin
            );
            var expectResult = new ListResult<>(plugins);

            when(client.listBy(same(Plugin.class), any(ListOptions.class), any(PageRequest.class)))
                .thenReturn(Mono.just(expectResult));

            bindToRouterFunction(endpoint.endpoint())
                .build()
                .get().uri("/plugins?enabled=true")
                .exchange()
                .expectStatus().isOk();
        }

        @Test
        void shouldSortPluginsWhenCreationTimestampSet() {
            var expectPlugin =
                createPlugin("fake-plugin-2", "expected display name", "", true);
            var expectResult = new ListResult<>(List.of(expectPlugin));
            when(client.listBy(same(Plugin.class), any(ListOptions.class), any(PageRequest.class)))
                .thenReturn(Mono.just(expectResult));

            bindToRouterFunction(endpoint.endpoint())
                .build()
                .get().uri("/plugins?sort=creationTimestamp,desc")
                .exchange()
                .expectStatus().isOk();
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

            lenient().when(systemVersionSupplier.get()).thenReturn(Version.parse("0.0.0"));
            tempDirectory = Files.createTempDirectory("halo-test-plugin-upgrade-");
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

        @Test
        void updateJsonConfigTest() {
            Plugin plugin = createPlugin("fake-plugin");
            plugin.getSpec().setConfigMapName("fake-config-map");

            when(client.fetch(eq(Plugin.class), eq("fake-plugin"))).thenReturn(Mono.just(plugin));
            when(settingConfigService.upsertConfig(eq("fake-config-map"), any()))
                .thenReturn(Mono.empty());

            webClient.put()
                .uri("/plugins/fake-plugin/json-config")
                .body(Mono.just(Map.of()), Map.class)
                .exchange()
                .expectStatus().is2xxSuccessful();
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

        @Test
        void fetchJsonConfig() {
            Plugin plugin = createPlugin("fake");
            plugin.getSpec().setConfigMapName("fake-config");

            when(settingConfigService.fetchConfig(eq("fake-config")))
                .thenReturn(Mono.empty());
            when(client.fetch(eq(Plugin.class), eq("fake"))).thenReturn(Mono.just(plugin));
            webClient.get()
                .uri("/plugins/fake/json-config")
                .exchange()
                .expectStatus().isOk();

            verify(settingConfigService).fetchConfig(eq("fake-config"));
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

    @Nested
    class BundleResourceEndpointTest {

        private long lastModified;

        WebTestClient webClient;

        @BeforeEach
        void setUp() {
            webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint()).build();
            long currentTimeMillis = System.currentTimeMillis();
            // We should ignore milliseconds here
            // See https://datatracker.ietf.org/doc/html/rfc7231#section-7.1.1.1 for more.
            this.lastModified = currentTimeMillis - currentTimeMillis % 1_000;
        }

        @Test
        void shouldBeRedirectedWhileFetchingBundleJsWithoutVersion() {
            when(pluginService.generateBundleVersion()).thenReturn(Mono.just("fake-version"));
            webClient.get().uri("/plugins/-/bundle.js")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().cacheControl(CacheControl.noStore())
                .expectHeader().location(
                    "/apis/api.console.halo.run/v1alpha1/plugins/-/bundle.js?v=fake-version");
        }

        @Test
        void shouldBeRedirectedWhileFetchingBundleCssWithoutVersion() {
            when(pluginService.generateBundleVersion()).thenReturn(Mono.just("fake-version"));
            webClient.get().uri("/plugins/-/bundle.css")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().cacheControl(CacheControl.noStore())
                .expectHeader().location(
                    "/apis/api.console.halo.run/v1alpha1/plugins/-/bundle.css?v=fake-version");
        }

        @Test
        void shouldFetchBundleCssWithCacheControl() {
            var cache = webProperties.getResources().getCache();
            cache.setUseLastModified(true);
            var cachecontrol = cache.getCachecontrol();
            cachecontrol.setNoCache(true);
            endpoint.afterPropertiesSet();

            when(pluginService.getCssBundle("fake-version"))
                .thenReturn(Mono.fromSupplier(() -> mockResource("fake-css")));
            webClient.get().uri("/plugins/-/bundle.css?v=fake-version")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.noCache())
                .expectHeader().contentType("text/css")
                .expectHeader().lastModified(lastModified)
                .expectBody(String.class).isEqualTo("fake-css");
        }

        @Test
        void shouldFetchBundleJsWithCacheControl() {
            var cache = webProperties.getResources().getCache();
            cache.setUseLastModified(true);
            var cachecontrol = cache.getCachecontrol();
            cachecontrol.setNoStore(true);
            endpoint.afterPropertiesSet();

            when(pluginService.getJsBundle("fake-version"))
                .thenReturn(Mono.fromSupplier(() -> mockResource("fake-js")));
            webClient.get().uri("/plugins/-/bundle.js?v=fake-version")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.noStore())
                .expectHeader().contentType("text/javascript")
                .expectHeader().lastModified(lastModified)
                .expectBody(String.class).isEqualTo("fake-js");
        }

        @Test
        void shouldFetchBundleCss() {
            when(pluginService.getCssBundle("fake-version"))
                .thenReturn(Mono.fromSupplier(() -> mockResource("fake-css")));
            webClient.get().uri("/plugins/-/bundle.css?v=fake-version")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.empty())
                .expectHeader().contentType("text/css")
                .expectHeader().lastModified(-1)
                .expectBody(String.class).isEqualTo("fake-css");
        }

        @Test
        void shouldFetchBundleJs() {
            when(pluginService.getJsBundle("fake-version"))
                .thenReturn(Mono.fromSupplier(() -> mockResource("fake-js")));
            webClient.get().uri("/plugins/-/bundle.js?v=fake-version")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.empty())
                .expectHeader().contentType("text/javascript")
                .expectHeader().lastModified(-1)
                .expectBody(String.class).isEqualTo("fake-js");
        }

        Resource mockResource(String content) {
            var resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
            resource = spy(resource);
            try {
                doReturn(lastModified).when(resource).lastModified();
            } catch (IOException e) {
                // should never happen
                throw new RuntimeException(e);
            }
            return resource;
        }
    }

}
