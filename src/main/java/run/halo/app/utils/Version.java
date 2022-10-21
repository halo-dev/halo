package run.halo.app.utils;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.model.support.HaloConst;

/**
 * Version domain.
 *
 * @author johnniang
 */
@Getter
@ToString
@EqualsAndHashCode
@Slf4j
public class Version {

    /**
     * Regex expression.
     */
    private static final String REGEX = "^"
        + "(?<major>0|[1-9]\\d*)\\."  // major number
        + "(?<minor>0|[1-9]\\d*)\\."  // minor number
        + "(?<patch>0|[1-9]\\d*)"  // patch number
        + "(?:-"  // pre-release start
        + "(?<preRelease>beta|alpha|rc)\\."  // pre-release type
        + "(?<preReleaseMajor>0|[1-9]\\d*)"  // pre-release major number
        + ")?$"; // pre-release end

    /**
     * Pattern.
     */
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    /**
     * Empty version.
     */
    private static final Version EMPTY_VERSION = new Version(0, 0, 0);

    /**
     * Maximum version.
     */
    private static final Version MAXIMUM_VERSION =
        new Version(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);

    /**
     * Major number.
     */
    private final long major;

    /**
     * Minor number.
     */
    private final long minor;

    /**
     * Patch number.
     */
    private final long patch;

    /**
     * Pre-release.
     */
    private final PreRelease preRelease;

    /**
     * Pre-release major number.
     */
    private final long preReleaseMajor;

    public Version() {
        this(0, 0, 0);
    }

    public Version(long major, long minor, long patch) {
        this(major, minor, patch, null, null);
    }

    public Version(long major, long minor, long patch, @Nullable PreRelease preRelease,
        @Nullable Long preReleaseMajor) {
        if (major < 0) {
            major = 0L;
        }
        if (minor < 0L) {
            minor = 0;
        }
        if (patch < 0) {
            minor = 0L;
        }
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.preRelease = preRelease;
        if (preRelease != null) {
            preReleaseMajor = preReleaseMajor == null ? Integer.MAX_VALUE : preReleaseMajor;
            if (preReleaseMajor < 0) {
                preReleaseMajor = 0L;
            }
            this.preReleaseMajor = preReleaseMajor;
        } else {
            this.preReleaseMajor = Integer.MAX_VALUE;
        }
    }

    /**
     * Empty version.
     *
     * @return empty version
     */
    @NonNull
    public static Version emptyVersion() {
        return EMPTY_VERSION;
    }

    /**
     * Resolve version.
     *
     * @param version version could be blank
     * @return an optional corresponding version
     */
    @NonNull
    public static Optional<Version> resolve(@Nullable String version) {
        if (StringUtils.isBlank(version)) {
            return Optional.empty();
        }
        // handle unknown version
        if (StringUtils.equalsIgnoreCase(version, HaloConst.UNKNOWN_VERSION)) {
            return Optional.of(MAXIMUM_VERSION);
        }
        // get matcher for version
        Matcher matcher = PATTERN.matcher(version);
        if (!matcher.matches()) {
            // if mismatches
            log.warn("Version: [{}] didn't match version format", version);
            return Optional.empty();
        }
        // get all groups
        String major = matcher.group("major");
        String minor = matcher.group("minor");
        String patch = matcher.group("patch");
        String preRelease = matcher.group("preRelease");
        String preReleaseMajor = matcher.group("preReleaseMajor");
        // build full version
        return Optional.of(new Version(Long.parseLong(major),
            Long.parseLong(minor),
            Long.parseLong(patch),
            PreRelease.of(preRelease),
            StringUtils.isNotBlank(preReleaseMajor) ? Long.parseLong(preReleaseMajor) : null));
    }

    /**
     * Check if the current version is compatible with the target version.
     *
     * @param target target version must not be blank.
     * @return true if the current version is compatible with the target version; false otherwise.
     */
    public boolean compatible(String target) {
        Version targetVersion = resolve(target).orElse(emptyVersion());
        // compare major
        int majorCompare = Long.compare(major, targetVersion.major);
        if (majorCompare != 0) {
            return majorCompare > 0;
        }
        // compare minor
        int minorCompare = Long.compare(minor, targetVersion.minor);
        if (minorCompare != 0) {
            return minorCompare > 0;
        }
        // compare patch
        int patchCompare = Long.compare(patch, targetVersion.patch);
        if (patchCompare != 0) {
            return patchCompare > 0;
        }

        // here means that all major, minor and patch number are the same.
        if (!this.isPreRelease() || !targetVersion.isPreRelease()) {
            return true;
        }
        // compare pre-release tag
        int preReleaseTagCompare =
            PreRelease.ALPHA.compare(
                Objects.requireNonNull(this.preRelease),
                Objects.requireNonNull(targetVersion.preRelease));
        if (preReleaseTagCompare != 0) {
            return preReleaseTagCompare > 0;
        }

        // compare pre-release number
        long preReleaseNumberCompare = this.preReleaseMajor - targetVersion.preReleaseMajor;
        if (preReleaseNumberCompare != 0) {
            return preReleaseNumberCompare > 0;
        }
        return true;
    }


    /**
     * Check if current version is a pre-release version.
     *
     * @return true if current version is a pre-release version; false otherwise.
     */
    public boolean isPreRelease() {
        return preRelease != null;
    }

    /**
     * Pre release enum.
     *
     * @author johnniang
     */
    public enum PreRelease implements Comparator<PreRelease> {

        /**
         * Alpha.
         */
        ALPHA(1),

        /**
         * Beta.
         */
        BETA(2),

        /**
         * Release candidate.
         */
        RC(3);

        /**
         * Lower priority means the pre-release is earlier than others.
         * Compare order: ALPHA < BETA < RC
         */
        private final int priority;

        PreRelease(int priority) {
            this.priority = priority;
        }

        @Nullable
        static PreRelease of(@Nullable String preReleaseStr) {
            PreRelease[] preReleases = PreRelease.values();
            for (PreRelease preRelease : preReleases) {
                if (preRelease.name().equalsIgnoreCase(preReleaseStr)) {
                    return preRelease;
                }
            }
            return null;
        }

        @Override
        public int compare(PreRelease left, PreRelease right) {
            return left.priority - right.priority;
        }
    }
}
