package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginWrapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Unstructured;
import run.halo.app.plugin.event.HaloPluginBeforeStopEvent;

class PluginBeforeStopSyncListenerTest {
    @Test
    void rootShutdownMustDeleteBundledResourcesButPreserveDoNotOverwriteResources() {
        var client = mock(ReactiveExtensionClient.class);
        var gvk = new GroupVersionKind("auth.halo.run", "v1alpha1", "AuthProvider");
        var provider = new Unstructured(Map.of(
                "apiVersion",
                "auth.halo.run/v1alpha1",
                "kind",
                "AuthProvider",
                "metadata",
                Map.of("name", "plugin-login")));
        var retained = new Unstructured(Map.of(
                "apiVersion",
                "auth.halo.run/v1alpha1",
                "kind",
                "AuthProvider",
                "metadata",
                Map.of("name", "retained", "labels", Map.of(ExtensionUtil.DO_NOT_OVERWRITE_LABEL, "true"))));
        var stored = new AtomicReference<>(provider);
        when(client.fetch(gvk, "plugin-login")).thenAnswer(ignored -> Mono.justOrEmpty(stored.get()));
        when(client.fetch(gvk, "retained")).thenReturn(Mono.just(retained));
        when(client.delete(provider)).thenReturn(Mono.fromSupplier(() -> stored.getAndSet(null)));
        var mapping = new LinkedMultiValueMap<GroupVersionKind, String>();
        mapping.add(gvk, "plugin-login");
        mapping.add(gvk, "retained");
        var pluginContext = mock(PluginApplicationContext.class);
        when(pluginContext.extensionNamesMapping()).thenReturn(mapping);
        var plugin = mock(DefaultSpringPlugin.class);
        when(plugin.getApplicationContext()).thenReturn(pluginContext);
        var wrapper = mock(PluginWrapper.class);
        when(wrapper.getPlugin()).thenReturn(plugin);
        try (var root = new AnnotationConfigApplicationContext()) {
            root.registerBean(PluginBeforeStopSyncListener.class, () -> new PluginBeforeStopSyncListener(client));
            root.refresh();
            root.publishEvent(new ContextClosedEvent(root));
            root.publishEvent(new HaloPluginBeforeStopEvent(this, wrapper));
        }
        assertThat(stored.get()).isNull();
        verify(client).delete(provider);
        verify(client, never()).delete(retained);
    }
}
