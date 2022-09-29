package run.halo.app.core.extension.endpoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.PluginProperties;

@ExtendWith(MockitoExtension.class)
class PluginEndpointTest {

    @Mock
    PluginProperties pluginProperties;

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    PluginEndpoint endpoint;

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
        var unexpectedPlugin1 = createPlugin("fake-plugin-1", "first fake display name", "", false);
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
        var unexpectedPlugin1 = createPlugin("fake-plugin-1", "first fake display name", "", false);
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
        var unexpectedPlugin1 = createPlugin("fake-plugin-1", "first fake display name", "", false);
        var unexpectedPlugin2 =
            createPlugin("fake-plugin-3", "second fake display name", "", false);
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
