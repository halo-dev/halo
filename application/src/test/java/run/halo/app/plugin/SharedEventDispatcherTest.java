package run.halo.app.plugin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.Lifecycle;

@ExtendWith(MockitoExtension.class)
class SharedEventDispatcherTest {

    @Mock
    PluginManager pluginManager;

    @Mock
    ApplicationEventPublisher publisher;

    @InjectMocks
    SharedEventDispatcher dispatcher;

    @Test
    void shouldNotDispatchEventIfNotSharedEvent() {
        dispatcher.onApplicationEvent(new FakeEvent(this));
        verify(pluginManager, never()).getStartedPlugins();
    }

    @Test
    void shouldDispatchEventToAllStartedPlugins() {
        var pw = mock(PluginWrapper.class);
        var plugin = mock(SpringPlugin.class);
        var context =
            mock(ApplicationContext.class, withSettings().extraInterfaces(Lifecycle.class));
        when(((Lifecycle) context).isRunning()).thenReturn(true);
        when(plugin.getApplicationContext()).thenReturn(context);
        when(pw.getPlugin()).thenReturn(plugin);
        when(pluginManager.getStartedPlugins()).thenReturn(List.of(pw));

        var event = new FakeSharedEvent(this);
        dispatcher.onApplicationEvent(event);

        verify(context).publishEvent(new HaloSharedEventDelegator(dispatcher, event));
    }

    @Test
    void shouldNotDispatchEventToAllStartedPluginsWhilePluginContextIsNotRunning() {
        var pw = mock(PluginWrapper.class);
        var plugin = mock(SpringPlugin.class);
        var context =
            mock(ApplicationContext.class, withSettings().extraInterfaces(Lifecycle.class));
        when(((Lifecycle) context).isRunning()).thenReturn(false);
        when(plugin.getApplicationContext()).thenReturn(context);
        when(pw.getPlugin()).thenReturn(plugin);
        when(pluginManager.getStartedPlugins()).thenReturn(List.of(pw));
        var event = new FakeSharedEvent(this);
        dispatcher.onApplicationEvent(event);
        verify(context, never()).publishEvent(event);
    }

    @Test
    void shouldNotDispatchEventToAllStartedPluginsWhilePluginContextIsNotLifecycle() {
        var pw = mock(PluginWrapper.class);
        var plugin = mock(SpringPlugin.class);
        var context = mock(ApplicationContext.class);
        when(plugin.getApplicationContext()).thenReturn(context);
        when(pw.getPlugin()).thenReturn(plugin);
        when(pluginManager.getStartedPlugins()).thenReturn(List.of(pw));
        var event = new FakeSharedEvent(this);
        dispatcher.onApplicationEvent(event);
        verify(context, never()).publishEvent(event);
    }

    @Test
    void shouldUnwrapPluginSharedEventAndRepublish() {
        var event = new PluginSharedEventDelegator(this, new FakeSharedEvent(this));
        dispatcher.onApplicationEvent(event);
        verify(publisher).publishEvent(event.getDelegate());
    }

    class FakeEvent extends ApplicationEvent {

        public FakeEvent(Object source) {
            super(source);
        }

    }

    @SharedEvent
    class FakeSharedEvent extends ApplicationEvent {

        public FakeSharedEvent(Object source) {
            super(source);
        }

    }
}