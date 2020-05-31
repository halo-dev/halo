package run.halo.app.utils;


import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

@Slf4j
public class GithubUtilsTest {

    @Test
    public void getLatestReleasesWithValidURL() {
        Map<String, Object> map = GithubUtils.getLatestRelease("https://github.com/halo-dev/halo-theme-hux");
    }

    @Test
    public void getLatestReleasesWithInvalidURL() {
        Map<String, Object> map = GithubUtils.getLatestRelease("https://github.com/halo-dev/halo-theme-hu");
        Assert.assertNull(map);
    }

    @Test
    public void accessThemePropertyWithValidURL() {
        String content = GithubUtils.accessThemeProperty("https://github.com/halo-dev/halo-theme-hux", "master");
    }

    @Test
    public void accessThemePropertyWithInvalidURL() {
        String content = GithubUtils.accessThemeProperty("https://github.com/halo-dev/halo-theme-hu", "master");
        Assert.assertNull(content);
    }

    @Test
    public void getReleasesTest() {
        List<String> list = GithubUtils.getReleases("https://github.com/halo-dev/halo-theme-hux");
    }
}
