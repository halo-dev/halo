package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.EventListener;
import run.halo.app.localization.LocalizableResourceChangedEvent;
import run.halo.app.localization.ResourceReference;

class LocalizableResourceChangedEventTest {
    @Test
    void ownerPluginEventReachesAnotherPluginThroughTheExistingSharedDispatcherOnce() {
        var manager = mock(SpringPluginManager.class);
        try (var root = new AnnotationConfigApplicationContext();
                var owner = new PluginApplicationContext("owner", manager, new DefaultListableBeanFactory());
                var translations =
                        new PluginApplicationContext("translations", manager, new DefaultListableBeanFactory())) {
            when(manager.getRootContext()).thenReturn(root);
            root.registerBean(SharedEventDispatcher.class, () -> new SharedEventDispatcher(manager, root));
            root.refresh();
            owner.refresh();
            owner.start();
            translations.registerBean(RecordedEvents.class);
            translations.refresh();
            translations.start();
            var plugins = List.of(wrapper(owner), wrapper(translations));
            when(manager.startedPlugins()).thenReturn(plugins);
            var event = new LocalizableResourceChangedEvent(
                    ResourceReference.external("shop", "product", "42"),
                    LocalizableResourceChangedEvent.Change.UPDATED);

            owner.publishEvent(event);

            assertThat(translations.getBean(RecordedEvents.class).events).containsExactly(event);
        }
    }

    private static PluginWrapper wrapper(PluginApplicationContext context) {
        var wrapper = mock(PluginWrapper.class);
        var plugin = mock(Plugin.class, withSettings().extraInterfaces(SpringPlugin.class));
        when(wrapper.getPlugin()).thenReturn(plugin);
        when(((SpringPlugin) plugin).getApplicationContext()).thenReturn(context);
        return wrapper;
    }

    static class RecordedEvents {
        final List<LocalizableResourceChangedEvent> events = new ArrayList<>();

        @EventListener
        void onChanged(LocalizableResourceChangedEvent event) {
            events.add(event);
        }
    }
}
