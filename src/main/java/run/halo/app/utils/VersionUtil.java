package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * Version utility.
 *
 * @author ryanwang
 * @author johnniang
 * @date 2020-02-03
 * @date 2020-08-03
 */
@Slf4j
public final class VersionUtil {

    private VersionUtil() {
    }

    /**
     * Compare two versions to see if they have the same major and minor versions.
     *
     * @param left version A to compare
     * @param right version B to compare
     * @return {@code true} if they have the same major and minor version.
     */
    public static boolean hasSameMajorAndMinorVersion(String left, String right) {
        Version leftVersion = Version.resolve(left).orElse(Version.emptyVersion());
        Version rightVersion = Version.resolve(right).orElse(Version.emptyVersion());
        return leftVersion.getMajor() == rightVersion.getMajor()
            && leftVersion.getMinor() == rightVersion.getMinor();
    }
}
