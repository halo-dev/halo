package run.halo.app.plugin.loader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.ClassLoadingStrategy;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;

@Slf4j
public class HaloPluginClassLoader extends PluginClassLoader {

    /**
     * see also <a href="https://github.com/halo-dev/halo/issues/4610">gh-4610</a>.
     */
    private final ClassLoadingStrategy resourceLoadingStrategy = ClassLoadingStrategy.PDA;

    public HaloPluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor,
        ClassLoader parent) {
        super(pluginManager, pluginDescriptor, parent, ClassLoadingStrategy.APD);
    }

    @Override
    public URL getResource(String name) {
        for (ClassLoadingStrategy.Source classLoadingSource :
            resourceLoadingStrategy.getSources()) {
            URL url = switch (classLoadingSource) {
                case APPLICATION -> super.getResource(name);
                case PLUGIN -> this.findResource(name);
                case DEPENDENCIES -> this.findResourceFromDependencies(name);
            };

            if (url != null) {
                log.trace("Found resource '{}' in {} classpath", name,
                    classLoadingSource);
                return url;
            }

            log.trace("Couldn't find resource '{}' in {}", name,
                classLoadingSource);
        }

        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> resources = new ArrayList<>();
        log.trace("Received request to load resources '{}'", name);

        for (ClassLoadingStrategy.Source classLoadingSource :
            resourceLoadingStrategy.getSources()) {
            switch (classLoadingSource) {
                case APPLICATION:
                    if (this.getParent() != null) {
                        resources.addAll(
                            Collections.list(this.getParent().getResources(name)));
                    }
                    break;
                case PLUGIN:
                    resources.addAll(Collections.list(this.findResources(name)));
                    break;
                case DEPENDENCIES:
                    resources.addAll(this.findResourcesFromDependencies(name));
                    break;
                default:
                    // Do nothing
            }
        }

        return Collections.enumeration(resources);
    }
}
