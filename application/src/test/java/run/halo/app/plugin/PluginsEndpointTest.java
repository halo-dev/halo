package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import run.halo.app.core.endpoint.WebSocketEndpoint;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.theme.finders.Finder;

class PluginsEndpointTest {

    @Test
    void shouldExposePluginsWebEndpoint() {
        var webEndpoint = PluginsEndpoint.class.getAnnotation(WebEndpoint.class);

        assertThat(webEndpoint).isNotNull();
        assertThat(webEndpoint.id()).isEqualTo("plugins");
    }

    @Test
    void shouldListStartedPluginRuntimeInfo() {
        var pluginManager = mock(SpringPluginManager.class);
        var pluginGetter = mock(PluginGetter.class);
        var context = pluginApplicationContext(pluginManager);
        try {
            var plugin = springPlugin(context);
            var pluginWrapper = pluginWrapper(plugin);
            var pluginExtension = pluginExtension();
            var endpoint = new PluginsEndpoint(pluginManager, pluginGetter);

            when(pluginManager.startedPlugins()).thenReturn(List.of(pluginWrapper));
            when(pluginManager.getExtensionClassNames("fake-plugin"))
                    .thenReturn(Set.of("run.halo.fake.FakeComponent"));
            when(pluginGetter.getPlugin("fake-plugin")).thenReturn(pluginExtension);

            var runtimeInfo = endpoint.plugins();

            assertThat(runtimeInfo).hasSize(1);
            assertThat(runtimeInfo.get(0))
                    .returns("fake-plugin", PluginsEndpoint.PluginRuntimeInfo::pluginName)
                    .returns("Fake Plugin", PluginsEndpoint.PluginRuntimeInfo::displayName)
                    .returns("1.0.0", PluginsEndpoint.PluginRuntimeInfo::version)
                    .returns(PluginState.STARTED.toString(), PluginsEndpoint.PluginRuntimeInfo::state)
                    .returns(
                            PluginsEndpointTest.class
                                    .getClassLoader()
                                    .getClass()
                                    .getName(),
                            PluginsEndpoint.PluginRuntimeInfo::classLoaderName)
                    .returns(1, PluginsEndpoint.PluginRuntimeInfo::loadedExtensionClassCount)
                    .returns(1, PluginsEndpoint.PluginRuntimeInfo::routerFunctionCount)
                    .returns(1, PluginsEndpoint.PluginRuntimeInfo::finderCount)
                    .returns(1, PluginsEndpoint.PluginRuntimeInfo::websocketEndpointCount);
            assertThat(runtimeInfo.get(0).beanDefinitionCount()).isGreaterThan(0);
            assertThat(runtimeInfo.get(0).singletonBeanCount()).isGreaterThan(0);
            assertThat(runtimeInfo.get(0).extensionMappings()).containsEntry("fake.halo.run/v1alpha1/Fake", 2);
        } finally {
            context.close();
        }
    }

    @Test
    void shouldFallbackWhenPluginApplicationContextIsUnavailable() {
        var pluginManager = mock(SpringPluginManager.class);
        var pluginGetter = mock(PluginGetter.class);
        var plugin = mock(org.pf4j.Plugin.class, withSettings().extraInterfaces(SpringPlugin.class));
        when(((SpringPlugin) plugin).getApplicationContext()).thenThrow(IllegalStateException.class);
        var pluginWrapper = pluginWrapper(plugin);
        var pluginExtension = pluginExtension();
        var endpoint = new PluginsEndpoint(pluginManager, pluginGetter);

        when(pluginManager.startedPlugins()).thenReturn(List.of(pluginWrapper));
        when(pluginManager.getExtensionClassNames("fake-plugin")).thenReturn(Set.of());
        when(pluginGetter.getPlugin("fake-plugin")).thenReturn(pluginExtension);

        var runtimeInfo = endpoint.plugins();

        assertThat(runtimeInfo).hasSize(1);
        assertThat(runtimeInfo.get(0))
                .returns(0, PluginsEndpoint.PluginRuntimeInfo::beanDefinitionCount)
                .returns(0, PluginsEndpoint.PluginRuntimeInfo::singletonBeanCount)
                .returns(0, PluginsEndpoint.PluginRuntimeInfo::routerFunctionCount)
                .returns(0, PluginsEndpoint.PluginRuntimeInfo::finderCount)
                .returns(0, PluginsEndpoint.PluginRuntimeInfo::websocketEndpointCount);
        assertThat(runtimeInfo.get(0).extensionMappings()).isEmpty();
    }

    @Test
    void shouldFallbackWhenPluginExtensionIsMissing() {
        var pluginManager = mock(SpringPluginManager.class);
        var pluginGetter = mock(PluginGetter.class);
        var plugin = mock(org.pf4j.Plugin.class);
        var pluginWrapper = pluginWrapper(plugin);
        var endpoint = new PluginsEndpoint(pluginManager, pluginGetter);

        when(pluginManager.startedPlugins()).thenReturn(List.of(pluginWrapper));
        when(pluginManager.getExtensionClassNames("fake-plugin")).thenReturn(Set.of());
        when(pluginGetter.getPlugin("fake-plugin")).thenThrow(new NotFoundException("Plugin not found"));

        var runtimeInfo = endpoint.plugins();

        assertThat(runtimeInfo).hasSize(1);
        assertThat(runtimeInfo.get(0))
                .returns("fake-plugin", PluginsEndpoint.PluginRuntimeInfo::pluginName)
                .returns(null, PluginsEndpoint.PluginRuntimeInfo::displayName);
    }

    private static PluginApplicationContext pluginApplicationContext(SpringPluginManager pluginManager) {
        var context = new PluginApplicationContext("fake-plugin", pluginManager, new DefaultListableBeanFactory());
        context.registerBean(TestConfiguration.class);
        context.refresh();
        context.addExtensionMapping(new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake"), "fake-one");
        context.addExtensionMapping(new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake"), "fake-two");
        return context;
    }

    private static org.pf4j.Plugin springPlugin(PluginApplicationContext context) {
        var plugin = mock(org.pf4j.Plugin.class, withSettings().extraInterfaces(SpringPlugin.class));
        when(((SpringPlugin) plugin).getApplicationContext()).thenReturn(context);
        return plugin;
    }

    private static PluginWrapper pluginWrapper(org.pf4j.Plugin plugin) {
        var pluginWrapper = mock(PluginWrapper.class);
        var descriptor = mock(PluginDescriptor.class);
        when(pluginWrapper.getPlugin()).thenReturn(plugin);
        when(pluginWrapper.getPluginId()).thenReturn("fake-plugin");
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STARTED);
        when(pluginWrapper.getPluginClassLoader()).thenReturn(PluginsEndpointTest.class.getClassLoader());
        when(pluginWrapper.getDescriptor()).thenReturn(descriptor);
        when(descriptor.getVersion()).thenReturn("1.0.0");
        return pluginWrapper;
    }

    private static Plugin pluginExtension() {
        var plugin = new Plugin();
        var spec = new Plugin.PluginSpec();
        spec.setDisplayName("Fake Plugin");
        plugin.setSpec(spec);
        return plugin;
    }

    static class TestConfiguration {

        @Bean
        RouterFunction<?> testRouterFunction() {
            return RouterFunctions.route()
                    .GET("/test", req -> org.springframework.web.reactive.function.server.ServerResponse.ok().build())
                    .build();
        }

        @Bean
        TestFinder testFinder() {
            return new TestFinder();
        }

        @Bean
        WebSocketEndpoint testWebSocketEndpoint() {
            return mock(WebSocketEndpoint.class);
        }
    }

    @Finder("testFinder")
    static class TestFinder {}
}
