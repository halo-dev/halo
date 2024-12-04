package run.halo.app.plugin;

import java.util.ArrayList;
import java.util.List;
import org.pf4j.PluginDependency;
import org.pf4j.PluginDescriptor;
import org.pf4j.util.DirectedGraph;

/**
 * <p>Pass in a list of started plugin names to resolve dependency relationships, and return a
 * list of plugin names that depend on the specified plugin.</p>
 * <p>Do not consider the problem of circular dependency, because all the plugins that have been
 * started must not have this problem.</p>
 */
public class OptionalDependentResolver {
    private final DirectedGraph<String> dependentsGraph;
    private final List<PluginDescriptor> plugins;

    public OptionalDependentResolver(List<PluginDescriptor> startedPlugins) {
        this.plugins = startedPlugins;
        this.dependentsGraph = new DirectedGraph<>();
        resolve();
    }

    private void resolve() {
        // populate graphs
        for (PluginDescriptor plugin : plugins) {
            addPlugin(plugin);
        }
    }

    public List<String> getOptionalDependents(String pluginId) {
        return new ArrayList<>(dependentsGraph.getNeighbors(pluginId));
    }

    private void addPlugin(PluginDescriptor descriptor) {
        String pluginId = descriptor.getPluginId();
        List<PluginDependency> dependencies = descriptor.getDependencies();
        if (dependencies.isEmpty()) {
            dependentsGraph.addVertex(pluginId);
        } else {
            boolean edgeAdded = false;
            for (PluginDependency dependency : dependencies) {
                if (dependency.isOptional()) {
                    edgeAdded = true;
                    dependentsGraph.addEdge(dependency.getPluginId(), pluginId);
                }
            }

            // Register the plugin without dependencies, if all of its dependencies are optional.
            if (!edgeAdded) {
                dependentsGraph.addVertex(pluginId);
            }
        }
    }
}
