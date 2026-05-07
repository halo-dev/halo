package run.halo.app.theme.dialect.expression;

import com.github.zafarkhaja.semver.Version;
import java.util.function.Supplier;
import run.halo.app.infra.utils.VersionUtils;

/**
 * Halo expression object for Thymeleaf templates.
 *
 * @author ruibaby
 * @since 2.25.0
 */
public class HaloExpressionObject {

    private final Supplier<Version> versionSupplier;

    public HaloExpressionObject(Supplier<Version> versionSupplier) {
        this.versionSupplier = versionSupplier;
    }

    /**
     * Checks if the current Halo version matches the specified SemVer constraint.
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code #halo.matchVersion('>=2.24.0')}</li>
     *   <li>{@code #halo.matchVersion('>2.0.0 & <3.0.0')}</li>
     * </ul>
     * <p>Note: development version {@code 0.0.0} always returns {@code true}.</p>
     *
     * @param constraint the SemVer expression string to check against
     * @return {@code true} if the current version satisfies the constraint
     */
    public boolean matchVersion(String constraint) {
        if (versionSupplier == null) {
            return false;
        }
        String version = versionSupplier.get().toString();
        if ("0.0.0".equals(version)) {
            return true;
        }
        return VersionUtils.checkVersionConstraint(version, constraint);
    }
}
