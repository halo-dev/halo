package run.halo.app.plugin;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.pf4j.ExtensionDescriptor;
import org.pf4j.ExtensionFactory;
import org.pf4j.ExtensionFinder;
import org.pf4j.ExtensionPoint;
import org.pf4j.ExtensionWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Extension finder from core application context.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ClassPathExtensionFinder implements ExtensionFinder {
    private static final String PLUGIN_ID = "system";
    private final ApplicationContext applicationContext;
    private final CoreExtensionFactory coreExtensionFactory;

    public ClassPathExtensionFinder(ApplicationContext applicationContext,
        CoreExtensionFactory coreExtensionFactory) {
        this.applicationContext = applicationContext;
        this.coreExtensionFactory = coreExtensionFactory;
    }

    @Override
    public <T> List<ExtensionWrapper<T>> find(Class<T> type) {
        return findFrom(type);
    }

    @Override
    public <T> List<ExtensionWrapper<T>> find(Class<T> type, String pluginId) {
        if (notCorePluginId(pluginId)) {
            return List.of();
        }
        return findFrom(type);
    }

    @Override
    public List<ExtensionWrapper> find(String pluginId) {
        if (notCorePluginId(pluginId)) {
            return List.of();
        }
        return applicationContext.getBeansOfType(ExtensionPoint.class).values()
            .stream()
            .map(bean -> (ExtensionWrapper) createExtensionWrapper(bean.getClass()))
            .toList();
    }

    @Override
    public Set<String> findClassNames(String pluginId) {
        if (notCorePluginId(pluginId)) {
            return Set.of();
        }
        return applicationContext.getBeansOfType(ExtensionPoint.class).values()
            .stream()
            .map(bean -> bean.getClass().getName())
            .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private <T> List<ExtensionWrapper<T>> findFrom(Class<T> type) {
        return applicationContext.getBeansOfType(type).values()
            .stream()
            .map(bean -> (ExtensionWrapper<T>) createExtensionWrapper(bean.getClass()))
            .toList();
    }

    private <T> ExtensionWrapper<T> createExtensionWrapper(Class<T> extensionClass) {
        ExtensionDescriptor descriptor = new ExtensionDescriptor(0, extensionClass);
        return new ExtensionWrapper<>(descriptor, coreExtensionFactory);
    }

    private boolean notCorePluginId(String pluginId) {
        return !PLUGIN_ID.equals(pluginId) && pluginId != null;
    }

    @Component
    public static class CoreExtensionFactory implements ExtensionFactory {
        private final ApplicationContext applicationContext;

        public CoreExtensionFactory(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        public <T> T create(Class<T> extensionClass) {
            return applicationContext.getBean(extensionClass);
        }
    }
}
