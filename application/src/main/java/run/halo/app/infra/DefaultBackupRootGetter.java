package run.halo.app.infra;

import java.nio.file.Path;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.HaloProperties;

@Component
public class DefaultBackupRootGetter implements BackupRootGetter {

    private final HaloProperties haloProperties;

    public DefaultBackupRootGetter(HaloProperties haloProperties) {
        this.haloProperties = haloProperties;
    }

    @Override
    public Path get() {
        return haloProperties.getWorkDir().resolve("backups");
    }
}
