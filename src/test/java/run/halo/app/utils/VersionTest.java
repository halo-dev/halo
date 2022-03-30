package run.halo.app.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static run.halo.app.utils.Version.PreRelease.ALPHA;
import static run.halo.app.utils.Version.PreRelease.BETA;
import static run.halo.app.utils.Version.PreRelease.RC;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Data;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;
import run.halo.app.model.support.HaloConst;

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
        "99999999999999999999999.999999999999999999.99999999999999999----RC-SNAPSHOT.12.09"
            + ".1--------------------------------..12",
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
        assertEquals(new Version(major, minor, patch, ALPHA, preReleaseMajor),
            versionOpt.get());

        version = major + "." + minor + "." + patch + "-beta." + preReleaseMajor;
        versionOpt = Version.resolve(version);
        assertTrue(versionOpt.isPresent());
        assertEquals(new Version(major, minor, patch, BETA, preReleaseMajor),
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
        assertEquals(new Version(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE),
            unknownVersionOpt.get());
    }

    @NonNull
    Version getVersion(String version) {
        return Version.resolve(version)
            .orElseThrow(() -> new IllegalArgumentException("Invalid version"));
    }

    @Test
    void compatibleTest() {
        @Data
        class Table {
            final String current;
            final String required;
            final boolean wantCompatible;
        }

        for (Table tt : new Table[] {
            // compatible
            new Table("1.5.0", "1.5.0", true),
            new Table("1.5.0", "1.4.0", true),
            new Table("1.5.0", "0.5.0", true),
            new Table("1.5.0-alpha.1", "1.5.0", true),
            new Table("1.5.0-beta.1", "1.5.0", true),
            new Table("1.5.0-rc.1", "1.5.0", true),
            new Table("1.5.0-alpha.2", "1.5.0-alpha.1", true),
            new Table("1.5.0-beta.1", "1.5.0-alpha.2", true),
            new Table("1.5.0-rc.1", "1.5.0-beta.2", true),
            new Table("1.5.0", "1.5.0-alpha.1", true),
            new Table("1.5.0", "1.5.0-beta.1", true),
            new Table("1.5.0", "1.5.0-rc.1", true),
            new Table("1.5.0", "1.5.0-rc.1", true),
            new Table("1.5.0", "", true),
            new Table("1.5.0", null, true),
            new Table(HaloConst.UNKNOWN_VERSION, "1.5.0", true),

            // incompatible
            new Table("1.5.0", "1.5.1", false),
            new Table("1.5.0", "1.6.0", false),
            new Table("1.5.0", "2.5.0", false),
            new Table("1.5.0-alpha.1", "1.5.0-alpha.2", false),
            new Table("1.5.0-alpha.2", "1.5.0-beta.1", false),
            new Table("1.5.0-beta.2", "1.5.0-rc.1", false),
        }) {
            Version current = Version.resolve(tt.current).orElse(null);
            assertNotNull(current);
            boolean compatible = current.compatible(tt.required);
            assertEquals(tt.wantCompatible, compatible,
                () -> String.format("Want: %s, but got compatible: %s", tt, compatible));
        }
    }

    @Test
    void isPreRelease() {
        assertFalse(new Version(0, 0, 0, null, 0L).isPreRelease());
        assertTrue(new Version(0, 0, 0, ALPHA, 0L).isPreRelease());
    }

    @Test
    void preReleaseTagCompare() {
        assertTrue(ALPHA.compare(ALPHA, BETA) < 0);
        assertTrue(ALPHA.compare(ALPHA, RC) < 0);
        assertTrue(ALPHA.compare(BETA, ALPHA) > 0);
        assertTrue(ALPHA.compare(BETA, RC) < 0);
        assertTrue(ALPHA.compare(RC, ALPHA) > 0);
        assertTrue(ALPHA.compare(RC, BETA) > 0);
        assertEquals(0, ALPHA.compare(ALPHA, ALPHA));
        assertEquals(0, ALPHA.compare(BETA, BETA));
        assertEquals(0, ALPHA.compare(RC, RC));
    }
}