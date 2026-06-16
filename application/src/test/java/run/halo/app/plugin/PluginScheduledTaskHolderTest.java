package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@ExtendWith(MockitoExtension.class)
class PluginScheduledTaskHolderTest {

    @Mock
    SpringPluginManager pluginManager;

    @Test
    void shouldExposeScheduledTasksFromStartedSpringPlugins() {
        var task1 = scheduledTask();
        var task2 = scheduledTask();
        var context = new AnnotationConfigApplicationContext();
        try {
            context.registerBean("holder1", ScheduledTaskHolder.class, () -> () -> Set.of(task1));
            context.registerBean("holder2", ScheduledTaskHolder.class, () -> () -> Set.of(task2));
            context.refresh();
            var plugin = springPlugin(context);
            var pluginWrapper = pluginWrapper(plugin);
            when(pluginManager.startedPlugins()).thenReturn(List.of(pluginWrapper));

            var holder = new PluginScheduledTaskHolder(pluginManager);

            assertThat(holder.getScheduledTasks()).containsExactly(task1, task2);
        } finally {
            context.close();
        }
    }

    @Test
    void shouldIgnoreNonSpringPlugins() {
        var pluginWrapper = pluginWrapper(mock(Plugin.class));
        when(pluginManager.startedPlugins()).thenReturn(List.of(pluginWrapper));

        var holder = new PluginScheduledTaskHolder(pluginManager);

        assertThat(holder.getScheduledTasks()).isEmpty();
    }

    @Test
    void shouldIgnoreUnavailablePluginApplicationContext() {
        var plugin = mock(Plugin.class, withSettings().extraInterfaces(SpringPlugin.class));
        when(((SpringPlugin) plugin).getApplicationContext()).thenThrow(IllegalStateException.class);
        var pluginWrapper = pluginWrapper(plugin);
        when(pluginManager.startedPlugins()).thenReturn(List.of(pluginWrapper));

        var holder = new PluginScheduledTaskHolder(pluginManager);

        assertThat(holder.getScheduledTasks()).isEmpty();
    }

    private static ScheduledTask scheduledTask() {
        var registrar = new ScheduledTaskRegistrar();
        return registrar.scheduleFixedRateTask(new FixedRateTask(() -> {}, Duration.ofSeconds(1), Duration.ZERO));
    }

    private static Plugin springPlugin(AnnotationConfigApplicationContext context) {
        var plugin = mock(Plugin.class, withSettings().extraInterfaces(SpringPlugin.class));
        when(((SpringPlugin) plugin).getApplicationContext()).thenReturn(context);
        return plugin;
    }

    private static PluginWrapper pluginWrapper(Plugin plugin) {
        var pluginWrapper = mock(PluginWrapper.class);
        when(pluginWrapper.getPlugin()).thenReturn(plugin);
        return pluginWrapper;
    }
}
