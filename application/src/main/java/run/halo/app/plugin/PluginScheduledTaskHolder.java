package run.halo.app.plugin;

import java.util.LinkedHashSet;
import java.util.Set;
import org.pf4j.PluginWrapper;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;

/** Exposes scheduled tasks from started plugins to Spring Boot Actuator. */
final class PluginScheduledTaskHolder implements ScheduledTaskHolder {

    private final SpringPluginManager pluginManager;

    PluginScheduledTaskHolder(SpringPluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public Set<ScheduledTask> getScheduledTasks() {
        var scheduledTasks = new LinkedHashSet<ScheduledTask>();
        for (PluginWrapper pluginWrapper : pluginManager.startedPlugins()) {
            if (pluginWrapper.getPlugin() instanceof SpringPlugin springPlugin) {
                scheduledTasks.addAll(getScheduledTasks(springPlugin));
            }
        }
        return scheduledTasks;
    }

    private Set<ScheduledTask> getScheduledTasks(SpringPlugin springPlugin) {
        try {
            var scheduledTasks = new LinkedHashSet<ScheduledTask>();
            springPlugin
                    .getApplicationContext()
                    .getBeansOfType(ScheduledTaskHolder.class, false, false)
                    .values()
                    .forEach(holder -> scheduledTasks.addAll(holder.getScheduledTasks()));
            return scheduledTasks;
        } catch (IllegalStateException e) {
            return Set.of();
        }
    }
}
