package run.halo.app.infra.utils;

import com.github.zafarkhaja.semver.Version;
import com.github.zafarkhaja.semver.expr.Expression;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.server.ServerWebInputException;

@UtilityClass
public class VersionUtils {

    /**
     * Check if this "requires" param satisfies for a given (system) version.
     *
     * @param version the version to check
     * @return true if version satisfies the "requires" or if requires was left blank
     */
    public static boolean satisfiesRequires(String version, String requires) {
        String requiresVersion = StringUtils.trim(requires);

        // an exact version x.y.z will implicitly mean the same as >=x.y.z
        if (requiresVersion.matches("^\\d+\\.\\d+\\.\\d+$")) {
            // If exact versions are not allowed in requires, rewrite to >= expression
            requiresVersion = ">=" + requiresVersion;
        }
        return version.equals("0.0.0") || checkVersionConstraint(version, requiresVersion);
    }

    /**
     * Checks if a version satisfies the specified SemVer {@link Expression} string.
     * If the constraint is empty or null then the method returns true.
     * Constraint examples: {@code >2.0.0} (simple), {@code ">=1.4.0 & <1.6.0"} (range).
     * See
     * <a href="https://github.com/zafarkhaja/jsemver#semver-expressions-api-ranges">semver-expressions-api-ranges</a> for more info.
     *
     * @param version the version to check
     * @param constraint the SemVer Expression string
     * @return true if version satisfies the constraint or if constraint was left blank
     */
    public static boolean checkVersionConstraint(String version, String constraint) {
        try {
            return StringUtils.isBlank(constraint)
                || "*".equals(constraint)
                || Version.valueOf(version).satisfies(constraint);
        } catch (Exception e) {
            throw new ServerWebInputException("Illegal requires version expression.", null, e);
        }
    }
}
