package run.halo.app.plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.pf4j.DevelopmentPluginRepository;
import org.springframework.util.CollectionUtils;

/**
 * <p>A {@link org.pf4j.PluginRepository} implementation that can add fixed plugin paths for
 * development {@link org.pf4j.RuntimeMode#DEVELOPMENT}.</p>
 * <p>change {@link #deletePluginPath(Path)} to a no-op method.</p>
 * Note: This class is not thread-safe.
 *
 * @author guqing
 * @since 2.0.0
 */
public class DefaultDevelopmentPluginRepository extends DevelopmentPluginRepository {
    private final List<Path> fixedPaths = new ArrayList<>();

    public DefaultDevelopmentPluginRepository(Path... pluginsRoots) {
        super(pluginsRoots);
    }

    public DefaultDevelopmentPluginRepository(List<Path> pluginsRoots) {
        super(pluginsRoots);
    }

    public void addFixedPath(Path path) {
        fixedPaths.add(path);
    }

    public void setFixedPaths(List<Path> paths) {
        if (CollectionUtils.isEmpty(paths)) {
            return;
        }
        fixedPaths.clear();
        fixedPaths.addAll(paths);
    }

    @Override
    public List<Path> getPluginPaths() {
        List<Path> paths = new ArrayList<>(fixedPaths);
        paths.addAll(super.getPluginPaths());
        return paths;
    }

    @Override
    public boolean deletePluginPath(Path pluginPath) {
        // do nothing
        return true;
    }
}
