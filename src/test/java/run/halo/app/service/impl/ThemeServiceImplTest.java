package run.halo.app.service.impl;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestTemplate;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.handler.theme.config.ThemeConfigResolver;
import run.halo.app.handler.theme.config.ThemePropertyResolver;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.service.OptionService;
import run.halo.app.utils.FileUtils;
import run.halo.app.utils.GitUtils;
import run.halo.app.utils.GithubUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GitUtils.class, GithubUtils.class, FileUtils.class, ThemeServiceImpl.class})
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
    private ThemePropertyResolver themePropertyResolver;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    public ThemeServiceImpl themeService;

    @Before
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
        themeService = PowerMockito.spy(new ThemeServiceImpl(haloProperties, optionService, cacheStore, themeConfigResolver, themePropertyResolver, restTemplate, eventPublisher));

        Mockito.doReturn(new ThemeProperty()).when(themePropertyResolver).resolve(Mockito.any(String.class));

        Mockito.doNothing().when(eventPublisher).publishEvent(Mockito.any(String.class));
    }

    @Test
    public void fetchGitTest() throws Exception {
        String uri = "https://github.com/halo-dev/halo-theme-pinghsu";
        PowerMockito.doNothing().when(themeService, "downloadZipAndUnzip", Mockito.any(String.class), Mockito.any(Path.class));
        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "add", Mockito.any(Path.class));
        ThemeProperty themeProperty = themeService.fetch(uri);
        Assert.assertNotNull(themeProperty);
    }

    @Test
    public void fetchZipTest() throws Exception {
        String uri = "https://github.com/halo-dev/halo-theme-pinghsu/archive/master.zip";
        PowerMockito.doNothing().when(themeService, "downloadZipAndUnzip", Mockito.any(String.class), Mockito.any(Path.class));
        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "add", Mockito.any(Path.class));
        ThemeProperty themeProperty = themeService.fetch(uri);
        Assert.assertNotNull(themeProperty);
    }

    @Test
    public void fetchBranchesTest() {
        String uri = "https://github.com/halo-dev/halo-theme-hux";

        List<ThemeProperty> themeProperties = themeService.fetchBranches(uri);

        Assert.assertNotNull(themeProperties);
        Assert.assertEquals(themeProperties.size(), 2);
    }


    @Test
    public void fetchBranchTest() throws Exception {
        String uri = "https://github.com/halo-dev/halo-theme-casper";
        String branch = "master";
        PowerMockito.doNothing().when(themeService, "downloadZipAndUnzip", Mockito.any(String.class), Mockito.any(Path.class));
        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "add", Mockito.any(Path.class));
        ThemeProperty themeProperty = themeService.fetchBranch(uri, branch);
        Assert.assertNotNull(themeProperty);
    }

    @Test
    public void fetchLatestReleaseTest() throws Exception {
        String uri = "https://github.com/halo-dev/halo-theme-casper";
        PowerMockito.doNothing().when(themeService, "downloadZipAndUnzip", Mockito.any(String.class), Mockito.any(Path.class));
        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "add", Mockito.any(Path.class));
        ThemeProperty themeProperty = themeService.fetchLatestRelease(uri);
        Assert.assertNotNull(themeProperty);
    }

    @Test
    public void updateTest() throws Exception {
        PowerMockito.doNothing().when(themeService, "downloadZipAndUnzip", Mockito.any(String.class), Mockito.any(Path.class));
        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "add", Mockito.any(Path.class));
        PowerMockito.doNothing().when(themeService, "pullFromGit", Mockito.any(ThemeProperty.class));
        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "getThemeOfNonNullBy", Mockito.any(String.class));
        ThemeProperty themeProperty = themeService.update("String");
        Assert.assertNotNull(themeProperty);
    }
}
