package run.halo.app.service.impl;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestTemplate;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.handler.theme.config.ThemeConfigResolver;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.service.OptionService;
import run.halo.app.utils.FileUtils;
import run.halo.app.utils.GitUtils;
import run.halo.app.utils.GithubUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GitUtils.class, GithubUtils.class, FileUtils.class, ThemeServiceImpl.class})
@Disabled("Due to power mockito dependency")
public class ThemeServiceImplTest {

    @Mock
    private HaloProperties haloProperties;

    @Mock
    private OptionService optionService;

    @Mock
    private AbstractStringCacheStore cacheStore;

    @Mock
    private ThemeConfigResolver themeConfigResolver;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    public ThemeServiceImpl themeService;

    @BeforeEach
    public void setUp() throws Exception {
        //Static Method
        PowerMockito.mockStatic(GithubUtils.class);
        PowerMockito.mockStatic(GitUtils.class);
        PowerMockito.mockStatic(FileUtils.class);

        PowerMockito.doReturn(Arrays.asList("master", "dev")).when(GitUtils.class, "getAllBranches", Mockito.any(String.class));
        PowerMockito.doNothing().when(GitUtils.class, "cloneFromGit", Mockito.any(String.class), Mockito.any(Path.class));

        PowerMockito.doReturn("propertyContent").when(GithubUtils.class, "accessThemeProperty", Mockito.any(String.class), Mockito.any(String.class));

        PowerMockito.doReturn(new File("tmpPath").toPath()).when(FileUtils.class, "createTempDirectory");
        PowerMockito.doNothing().when(FileUtils.class, "deleteFolderQuietly", Mockito.any(Path.class));

        //Method
        themeService = PowerMockito.spy(new ThemeServiceImpl(haloProperties, optionService, cacheStore, themeConfigResolver, restTemplate, eventPublisher));

        Mockito.doNothing().when(eventPublisher).publishEvent(Mockito.any(String.class));
    }

    @Test
    public void fetchGitTest() throws Exception {
        String uri = "https://github.com/halo-dev/halo-theme-pinghsu";
        PowerMockito.doNothing().when(themeService, "downloadZipAndUnzip", Mockito.any(String.class), Mockito.any(Path.class));
        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "add", Mockito.any(Path.class));
        ThemeProperty themeProperty = themeService.fetch(uri);
        Assertions.assertNotNull(themeProperty);
    }

    @Test
    public void fetchZipTest() throws Exception {
        String uri = "https://github.com/halo-dev/halo-theme-pinghsu/archive/master.zip";
        PowerMockito.doNothing().when(themeService, "downloadZipAndUnzip", Mockito.any(String.class), Mockito.any(Path.class));
        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "add", Mockito.any(Path.class));
        ThemeProperty themeProperty = themeService.fetch(uri);
        Assertions.assertNotNull(themeProperty);
    }

    @Test
    public void fetchBranchesTest() {
        String uri = "https://github.com/halo-dev/halo-theme-hux";

        List<ThemeProperty> themeProperties = themeService.fetchBranches(uri);

        Assertions.assertNotNull(themeProperties);
        Assertions.assertEquals(themeProperties.size(), 2);
    }


    @Test
    public void fetchBranchTest() throws Exception {
        String uri = "https://github.com/halo-dev/halo-theme-casper";
        String branch = "master";
        PowerMockito.doNothing().when(themeService, "downloadZipAndUnzip", Mockito.any(String.class), Mockito.any(Path.class));
        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "add", Mockito.any(Path.class));
        ThemeProperty themeProperty = themeService.fetchBranch(uri, branch);
        Assertions.assertNotNull(themeProperty);
    }

    @Test
    public void fetchLatestReleaseTest() throws Exception {
        String uri = "https://github.com/halo-dev/halo-theme-casper";
        PowerMockito.doNothing().when(themeService, "downloadZipAndUnzip", Mockito.any(String.class), Mockito.any(Path.class));
        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "add", Mockito.any(Path.class));
        ThemeProperty themeProperty = themeService.fetchLatestRelease(uri);
        Assertions.assertNotNull(themeProperty);
    }

    @Test
    public void updateTest() throws Exception {
        PowerMockito.doNothing().when(themeService, "downloadZipAndUnzip", Mockito.any(String.class), Mockito.any(Path.class));
        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "add", Mockito.any(Path.class));
        PowerMockito.doNothing().when(themeService, "pullFromGit", Mockito.any(ThemeProperty.class));
        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "getThemeOfNonNullBy", Mockito.any(String.class));
        ThemeProperty themeProperty = themeService.update("String");
        Assertions.assertNotNull(themeProperty);
    }
}
