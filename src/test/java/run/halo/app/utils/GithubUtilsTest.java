package run.halo.app.utils;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
@Disabled("Dut to time-consumption")
class GithubUtilsTest {

    @Test
    void getLatestReleasesWithValidURL() {
        Map<String, Object> map = GithubUtils.getLatestRelease("https://github.com/halo-dev/halo-theme-hux");
    }

    @Test
    void getLatestReleasesWithInvalidURL() {
        Map<String, Object> map = GithubUtils.getLatestRelease("https://github.com/halo-dev/halo-theme-hu");
        assertNull(map);
    }

    @Test
    void accessThemePropertyWithValidURL() {
        String content = GithubUtils.accessThemeProperty("https://github.com/halo-dev/halo-theme-hux", "master");
    }

    @Test
    void accessThemePropertyWithInvalidURL() {
        String content = GithubUtils.accessThemeProperty("https://github.com/halo-dev/halo-theme-hu", "master");
        assertNull(content);
    }

    @Test
    void getReleasesTest() {
        List<String> list = GithubUtils.getReleases("https://github.com/halo-dev/halo-theme-hux");
    }
}
