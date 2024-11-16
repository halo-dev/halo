package run.halo.app.core.attachment;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Gets the root path(work dir) of the local attachment.
 */
public interface AttachmentRootGetter extends Supplier<Path> {
}
