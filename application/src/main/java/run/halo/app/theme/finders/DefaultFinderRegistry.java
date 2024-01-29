package run.halo.app.theme.finders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Finder registry for class annotated with {@link Finder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class DefaultFinderRegistry implements FinderRegistry, InitializingBean {
    private final Map<String, List<String>> pluginFindersLookup = new ConcurrentHashMap<>();
    private final Map<String, Object> finders = new ConcurrentHashMap<>(64);

    private final ApplicationContext applicationContext;

    public DefaultFinderRegistry(ApplicationContext applicationContext) {
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
    void putFinder(String name, Object finder) {
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
    String putFinder(Object finder) {
        var name = getFinderName(finder);
        this.putFinder(name, finder);
        return name;
    }

    private String getFinderName(Object finder) {
        var annotation = finder.getClass().getAnnotation(Finder.class);
        if (annotation == null) {
            // should never happen
            throw new IllegalStateException("Finder must be annotated with @Finder");
        }
        String name = annotation.value();
        if (name == null) {
            name = finder.getClass().getSimpleName();
        }
        return name;
    }

    public void removeFinder(String name) {
        finders.remove(name);
    }

    public Map<String, Object> getFinders() {
        return Map.copyOf(finders);
    }

    @Override
    public void afterPropertiesSet() {
        // initialize finders from application context
        applicationContext.getBeansWithAnnotation(Finder.class)
            .forEach((beanName, finder) -> {
                var finderName = getFinderName(finder);
                this.putFinder(finderName, finder);
            });
    }

    @Override
    public void register(String pluginId, ApplicationContext pluginContext) {
        pluginContext.getBeansWithAnnotation(Finder.class)
            .forEach((beanName, finder) -> {
                var finderName = getFinderName(finder);
                this.putFinder(finderName, finder);
                pluginFindersLookup
                    .computeIfAbsent(pluginId, ignored -> new ArrayList<>())
                    .add(finderName);
            });
    }

    @Override
    public void unregister(String pluginId) {
        var finderNames = pluginFindersLookup.remove(pluginId);
        if (finderNames != null) {
            finderNames.forEach(finders::remove);
        }
    }

}
