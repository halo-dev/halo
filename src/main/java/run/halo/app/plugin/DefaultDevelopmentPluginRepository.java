package run.halo.app.plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.pf4j.DevelopmentPluginRepository;
import org.springframework.util.CollectionUtils;

/**
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
}
