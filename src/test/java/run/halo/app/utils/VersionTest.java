package run.halo.app.utils;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;
import run.halo.app.model.support.HaloConst;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Version resolve test.
 *
 * @author johnniang
 */
class VersionTest {

    /**
     * Invalid versions.
     */
    String[] invalidVersions = new String[] {
            null,
            "",
            "1",
            "1.2",
            "1.2.3-0123",
            "1.2.3-0123.0123",
            "1.1.2+.123",
            "+invalid",
            "-invalid",
            "-invalid+invalid",
            "-invalid.01",
            "alpha",
            "alpha.beta",
            "alpha.beta.1",
            "alpha.1",
            "alpha+beta",
            "alpha_beta",
            "alpha.",
            "alpha..",
            "beta",
            "1.0.0-alpha_beta",
            "-alpha.",
            "1.0.0-alpha..",
            "1.0.0-alpha..1",
            "1.0.0-alpha...1",
            "1.0.0-alpha....1",
            "1.0.0-alpha.....1",
            "1.0.0-alpha......1",
            "1.0.0-alpha.......1",
            "01.1.1",
            "1.01.1",
            "1.1.01",
            "1.2",
            "1.2.3.DEV",
            "1.2-SNAPSHOT",
            "1.2.31.2.3----RC-SNAPSHOT.12.09.1--..12+788",
            "1.2-RC-SNAPSHOT",
            "-1.0.3-gamma+b7718",
            "+justmeta",
            "9.8.7+meta+meta",
            "9.8.7-whatever+meta+meta",
            "99999999999999999999999.999999999999999999.99999999999999999----RC-SNAPSHOT.12.09.1--------------------------------..12",
            "1.0.0-0A.is.legal",
            "1.0.0-alpha+beta",
            "1.2.3----RC-SNAPSHOT.12.9.1--.12+788",
            "1.2.3----R-S.12.9.1--.12+meta",
            "1.2.3----RC-SNAPSHOT.12.9.1--.12",
            "1.0.0+0.build.1-rc.10000aaa-kk-0.1",
            "1.0.0-0A.is.legal",
            "2.0.0+build.1848",
            "1.0.0-alpha0.valid",
            "1.0.0-alpha.0valid",
            "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay",
            "1.0.0-rc.1+build.1",
            "2.0.0-rc.1+build.123",
            "1.2.3-beta",
            "10.2.3-DEV-SNAPSHOT",
            "1.2.3-SNAPSHOT-123",
            "1.1.2-prerelease+meta",
            "1.1.2+meta",
            "1.1.2+meta-valid",
            "1.0.0-alpha",
            "1.0.0-beta",
            "1.0.0-alpha.beta",
            "1.0.0-alpha.beta.1"
    };

    @Test
    void invalidVersionResolve() {
        Stream.of(invalidVersions).forEach(invalidVersion -> {
            Optional<Version> versionOpt = Version.resolve(invalidVersion);
            assertFalse(versionOpt.isPresent());
        });
    }

    @Test
    void releaseVersionResolve() {
        long major = RandomUtils.nextLong();
        long minor = RandomUtils.nextLong();
        long patch = RandomUtils.nextLong();
        String version = major + "." + minor + "." + patch;
        Optional<Version> versionOpt = Version.resolve(version);
        assertTrue(versionOpt.isPresent());
        assertEquals(new Version(major, minor, patch), versionOpt.get());
    }

    @Test
    void preReleaseVersionResolve() {
        long major = RandomUtils.nextLong();
        long minor = RandomUtils.nextLong();
        long patch = RandomUtils.nextLong();
        long preReleaseMajor = RandomUtils.nextLong();
        String version = major + "." + minor + "." + patch + "-alpha." + preReleaseMajor;
        Optional<Version> versionOpt = Version.resolve(version);
        assertTrue(versionOpt.isPresent());
        assertEquals(new Version(major, minor, patch, Version.PreRelease.ALPHA, preReleaseMajor),
                versionOpt.get());

        version = major + "." + minor + "." + patch + "-beta." + preReleaseMajor;
        versionOpt = Version.resolve(version);
        assertTrue(versionOpt.isPresent());
        assertEquals(new Version(major, minor, patch, Version.PreRelease.BETA, preReleaseMajor),
                versionOpt.get());

        version = major + "." + minor + "." + patch + "-rc." + preReleaseMajor;
        versionOpt = Version.resolve(version);
        assertTrue(versionOpt.isPresent());
        assertEquals(new Version(major, minor, patch, Version.PreRelease.RC, preReleaseMajor),
                versionOpt.get());
    }

    @Test
    void unknownVersionTest() {
        Optional<Version> unknownVersionOpt = Version.resolve(HaloConst.UNKNOWN_VERSION);
        assertEquals(new Version(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE), unknownVersionOpt.get());
    }

    @Test
    void compareTest() {
        final Version leftVersion = getVersion("1.2.3");
        // compare with own
        assertEquals(0, leftVersion.compareTo(leftVersion));

        // compare with others
        Version rightVersion = getVersion("1.2.4");
        assertTrue(leftVersion.compareTo(rightVersion) < 0);

        rightVersion = getVersion("1.3.3");
        assertTrue(leftVersion.compareTo(rightVersion) < 0);

        rightVersion = getVersion("2.2.3");
        assertTrue(leftVersion.compareTo(rightVersion) < 0);

        rightVersion = getVersion("0.2.3");
        assertTrue(leftVersion.compareTo(rightVersion) > 0);

        rightVersion = getVersion("1.1.3");
        assertTrue(leftVersion.compareTo(rightVersion) > 0);

        rightVersion = getVersion("1.2.2");
        assertTrue(leftVersion.compareTo(rightVersion) > 0);

        rightVersion = getVersion("1.2.3-alpha.0");
        assertTrue(leftVersion.compareTo(rightVersion) > 0);

        rightVersion = getVersion("1.2.4-alpha.0");
        assertTrue(leftVersion.compareTo(rightVersion) < 0);

        // compare with unknown version
        assertTrue(leftVersion.compareTo(getVersion(HaloConst.UNKNOWN_VERSION)) < 0);
    }

    @NonNull
    Version getVersion(String version) {
        return Version.resolve(version).orElseThrow(() -> new IllegalArgumentException("Invalid version"));
    }
}