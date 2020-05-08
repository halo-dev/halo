package run.halo.app.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.utils.GitUtils;
import run.halo.app.utils.HaloUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class ThemeServiceImplTest {

    @Autowired
    private ThemeServiceImpl themeService;

    @Test
    @Ignore
    public void fetchGitTest() throws IOException {
        ThemeProperty themeProperty = themeService.fetch("https://github.com/halo-dev/halo-theme-pinghsu");
        Assert.assertNotNull(themeProperty);
        Path themePath = Paths.get(themeProperty.getThemePath());
        if (!Files.exists(themePath)) {
            Assert.fail("fetch(clone from master) failed.");
        } else {
            deleteDir(themePath);
        }

    }

    @Test
    @Ignore
    public void fetchZipTest() throws IOException {
        ThemeProperty themeProperty = themeService.fetch("https://github.com/halo-dev/halo-theme-anatole/archive/master.zip");
        Assert.assertNotNull(themeProperty);
        Path themePath = Paths.get(themeProperty.getThemePath());
        if (!Files.exists(themePath)) {
            Assert.fail("fetch(zip file) failed");
        } else {
            deleteDir(themePath);
        }
    }

    @Test
    @Ignore
    public void fetchBranchesWithValidURL() {
        List<ThemeProperty> themeProperties = themeService.fetchBranches("https://github.com/halo-dev/halo-theme-hux");
        Assert.assertNotNull(themeProperties);
        Assert.assertNotEquals(themeProperties.size(), 0);
    }

    @Test
    @Ignore
    public void fetchBranchesWithInvalidURL() {
        List<ThemeProperty> themeProperties = themeService.fetchBranches("https://github.com/halo-dev/halo-theme");
        Assert.assertNotNull(themeProperties);
        Assert.assertEquals(themeProperties.size(), 0);

    }


    @Test
    @Ignore
    public void fetchBranchTest() throws IOException {
        ThemeProperty themeProperty = themeService.fetchBranch("https://github.com/halo-dev/halo-theme-casper", "master");
        Assert.assertNotNull(themeProperty);
        Path themePath = Paths.get(themeProperty.getThemePath());
        if (!Files.exists(themePath)) {
            Assert.fail("fetch(branch master) failed");
        } else {
            deleteDir(themePath);
        }
    }

    @Test
    @Ignore
    public void fetchBranchNotMasterTest() throws IOException {
        String uri = "https://github.com/halo-dev/halo-theme-casper";
        List<String> branches = GitUtils.getAllBranches(uri);
        Optional<String> optional = branches.stream().filter(b -> !b.equalsIgnoreCase("master")).findFirst();
        if (optional.isPresent()) {
            ThemeProperty themeProperty = themeService.fetchBranch(uri, optional.get());
            Assert.assertNotNull(themeProperty);
            Path themePath = Paths.get(themeProperty.getThemePath());
            if (!Files.exists(themePath)) {
                Assert.fail("fetch(branch not master) failed");
            } else {
                deleteDir(themePath);
            }
        }
    }

    @Test
    @Ignore
    public void fetchLatestReleaseTest() throws IOException {
        ThemeProperty themeProperty = themeService.fetchLatestRelease("https://github.com/halo-dev/halo-theme-next");
        Assert.assertNotNull(themeProperty);
        Path themePath = Paths.get(themeProperty.getThemePath());
        if (Files.exists(themePath)) {
            Assert.fail("fetch(latest release) failed");
        } else {
            deleteDir(themePath);
        }
    }

    @Test
    @Ignore
    public void fetchLatestReleaseWithInvalidURL() {
        try {
            ThemeProperty themeProperty = themeService.fetchLatestRelease("123");
            Assert.fail("Exception Expected");
        } catch (Exception e) {
        }
    }

    @Test
    @Ignore
    public void updateTest() {
        Set<ThemeProperty> set = themeService.getThemes();
        if (set.size() > 0) {
            ThemeProperty themeProperty = themeService.update(set.stream().findFirst().get().getId());
            Assert.assertNotNull(themeProperty);
            Path themePath = Paths.get(themeProperty.getThemePath());
            if (!Files.exists(themePath)) {
                Assert.fail("update fail: theme folder missing");
            }
        }
    }

    @Test
    @Ignore
    public void updateWithInvalidId() {
        Set<ThemeProperty> set = themeService.getThemes();
        String themeId = HaloUtils.randomUUIDWithoutDash();
        AtomicInteger has = new AtomicInteger();
        while (true) {
            set.stream().filter(themeProperty -> StringUtils.equalsIgnoreCase(themeId, themeProperty.getId())).mapToInt(themeProperty -> 1).forEach(has::set);
            if (has.get() == 0) break;
        }
        try {
            ThemeProperty themeProperty = themeService.update(themeId);
            Assert.fail("Exception expected.");
        } catch (Exception e) {
        }
    }


    private static void deleteDir(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Arrays.asList(path.toFile().listFiles()).stream().forEach(file -> {
                if (file.isDirectory()) {
                    try {
                        deleteDir(file.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    file.delete();
                }
            });
        }
        Files.deleteIfExists(path);
    }
}
