package run.halo.app.infra;

import com.github.zafarkhaja.semver.Version;
import java.util.function.Supplier;

/**
 * The supplier to gets the project version.
 * If it cannot be obtained, return 0.0.0.
 *
 * @author guqing
 * @see <a href="https://semver.org">Semantic Versioning 2.0.0</a>
 * @since 2.0.0
 */
public interface SystemVersionSupplier extends Supplier<Version> {
}
