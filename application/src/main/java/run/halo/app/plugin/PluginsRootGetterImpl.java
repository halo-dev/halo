package run.halo.app.plugin;

import java.nio.file.Path;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.HaloProperties;

/**
 * Default implementation of {@link PluginsRootGetter}.
 *
 * @author johnniang
 */
@Component
public class PluginsRootGetterImpl implements PluginsRootGetter {

    private final HaloProperties haloProperties;

    public PluginsRootGetterImpl(HaloProperties haloProperties) {
        this.haloProperties = haloProperties;
    }

    @Override
    public Path get() {
        return haloProperties.getWorkDir().resolve("plugins");
    }
}
