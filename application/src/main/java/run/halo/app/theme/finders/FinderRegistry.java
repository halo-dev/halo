package run.halo.app.theme.finders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import run.halo.app.plugin.ExtensionContextRegistry;
import run.halo.app.plugin.PluginApplicationContext;
import run.halo.app.plugin.event.HaloPluginStartedEvent;
import run.halo.app.plugin.event.HaloPluginStoppedEvent;

/**
 * Finder registry for class annotated with {@link Finder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class FinderRegistry implements InitializingBean {
    private final Map<String, List<String>> pluginFindersLookup = new ConcurrentHashMap<>();
    private final Map<String, Object> finders = new ConcurrentHashMap<>(64);

    private final ApplicationContext applicationContext;

    public FinderRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    Object get(String name) {
        return finders.get(name);
    }

    /**
     * Given a name, register a Finder for it.
     *
     * @param name the canonical name
     * @param finder the finder to be registered
     * @throws IllegalStateException if the name is already existing
     */
    public void registerFinder(String name, Object finder) {
        if (finders.containsKey(name)) {
            throw new IllegalStateException(
                "Finder with name '" + name + "' is already registered");
        }
        finders.put(name, finder);
    }

    /**
     * Register a finder.
     *
     * @param finder register a finder that annotated with {@link Finder}
     * @return the name of the finder
     */
    public String registerFinder(Object finder) {
        Finder annotation = finder.getClass().getAnnotation(Finder.class);
        if (annotation == null) {
            throw new IllegalStateException("Finder must be annotated with @Finder");
        }
        String name = annotation.value();
        if (name == null) {
            name = finder.getClass().getSimpleName();
        }
        this.registerFinder(name, finder);
        return name;
    }

    public void removeFinder(String name) {
        finders.remove(name);
    }

    public Map<String, Object> getFinders() {
        return Map.copyOf(finders);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // initialize finders from application context
        applicationContext.getBeansWithAnnotation(Finder.class)
            .forEach((k, v) -> {
                registerFinder(v);
            });
    }

    /**
     * Register finders for a plugin.
     *
     * @param event plugin started event
     */
    @EventListener(HaloPluginStartedEvent.class)
    public void onPluginStarted(HaloPluginStartedEvent event) {
        String pluginId = event.getPlugin().getPluginId();
        PluginApplicationContext pluginApplicationContext = ExtensionContextRegistry.getInstance()
            .getByPluginId(pluginId);
        pluginApplicationContext.getBeansWithAnnotation(Finder.class)
            .forEach((beanName, finderObject) -> {
                // register finder
                String finderName = registerFinder(finderObject);
                // add to plugin finder lookup
                pluginFindersLookup.computeIfAbsent(pluginId, k -> new ArrayList<>())
                    .add(finderName);
            });
    }

    /**
     * Remove finders registered by the plugin.
     *
     * @param event plugin stopped event
     */
    @EventListener(HaloPluginStoppedEvent.class)
    public void onPluginStopped(HaloPluginStoppedEvent event) {
        String pluginId = event.getPlugin().getPluginId();
        boolean containsKey = pluginFindersLookup.containsKey(pluginId);
        if (!containsKey) {
            return;
        }
        pluginFindersLookup.get(pluginId).forEach(this::removeFinder);
    }

    /**
     * Only for test.
     *
     * @param pluginId plugin id
     * @param finderName finder name
     */
    void addPluginFinder(String pluginId, String finderName) {
        pluginFindersLookup.computeIfAbsent(pluginId, k -> new ArrayList<>())
            .add(finderName);
    }
}
