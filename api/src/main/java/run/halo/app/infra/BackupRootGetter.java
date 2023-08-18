package run.halo.app.infra;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Utility of getting backup root path.
 *
 * @author johnniang
 * @since 2.9.0
 */
public interface BackupRootGetter extends Supplier<Path> {

}
