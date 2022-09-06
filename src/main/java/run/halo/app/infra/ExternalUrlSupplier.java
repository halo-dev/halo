package run.halo.app.infra;

import java.net.URI;
import java.util.function.Supplier;

/**
 * Represents a supplier of external url configuration.
 *
 * @author johnniang
 */
public interface ExternalUrlSupplier extends Supplier<URI> {

}
