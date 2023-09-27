package run.halo.app.plugin;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.ClassLoadingStrategy;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;

/**
 * <p>Plugin manager should create one instance of this class for every available plugin. By
 * default, this class loader is a Parent First ClassLoader - it loads the classes from the
 * parent class loader before loads from the plugin. Use classLoadingStrategy to change
 * the loading strategy.</p>
 * <p>By default, this class loader loads the resources from the plugin only - if the resource not
 * found in the plugin, it returns null.</p>
 *
 * @author guqing
 * @since 2.10.0
 */
@Slf4j
public class DefaultPluginClassLoader extends PluginClassLoader {
    public DefaultPluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor,
        ClassLoader parent) {
        super(pluginManager, pluginDescriptor, parent, ClassLoadingStrategy.APD);
    }

    @Override
    public URL getResource(String name) {
        log.trace("Received request to load resource '{}'", name);
        return this.findResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return this.findResources(name);
    }
}
