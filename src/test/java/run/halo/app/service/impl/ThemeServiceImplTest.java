package run.halo.app.service.impl;


import org.junit.jupiter.api.Disabled;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({GitUtils.class, GithubUtils.class, FileUtils.class, ThemeServiceImpl.class})
@Disabled("Due to power mockito dependency")
class ThemeServiceImplTest {
//
//    @Mock
//     HaloProperties haloProperties;
//
//    @Mock
//     OptionService optionService;
//
//    @Mock
//     AbstractStringCacheStore cacheStore;
//
//    @Mock
//     ThemeConfigResolver themeConfigResolver;
//
//    @Mock
//     RestTemplate restTemplate;
//
//    @Mock
//     ApplicationEventPublisher eventPublisher;
//
//    @InjectMocks
//     ThemeServiceImpl themeService;
//
//    @BeforeEach
//     void setUp() throws Exception {
//        //Static Method
//        PowerMockito.mockStatic(GithubUtils.class);
//        PowerMockito.mockStatic(GitUtils.class);
//        PowerMockito.mockStatic(FileUtils.class);
//
//        PowerMockito.doReturn(Arrays.asList("master", "dev")).when(GitUtils.class,
//        "getAllBranches", Mockito.any(String.class));
//        PowerMockito.doNothing().when(GitUtils.class, "cloneFromGit", Mockito.any(String.class)
//        , Mockito.any(Path.class));
//
//        PowerMockito.doReturn("propertyContent").when(GithubUtils.class, "accessThemeProperty",
//        Mockito.any(String.class), Mockito.any(String.class));
//
//        PowerMockito.doReturn(new File("tmpPath").toPath()).when(FileUtils.class,
//        "createTempDirectory");
//        PowerMockito.doNothing().when(FileUtils.class, "deleteFolderQuietly", Mockito.any(Path
//        .class));
//
//        //Method
//        themeService = PowerMockito.spy(new ThemeServiceImpl(haloProperties, optionService,
//        cacheStore, themeConfigResolver, restTemplate, eventPublisher));
//
//        Mockito.doNothing().when(eventPublisher).publishEvent(Mockito.any(String.class));
//    }
//
//    @Test
//     void fetchGitTest() throws Exception {
//        String uri = "https://github.com/halo-dev/halo-theme-pinghsu";
//        PowerMockito.doNothing().when(themeService, "downloadZipAndUnzip", Mockito.any(String
//        .class), Mockito.any(Path.class));
//        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "add", Mockito.any(Path
//        .class));
//        ThemeProperty themeProperty = themeService.fetch(uri);
//        Assertions.assertNotNull(themeProperty);
//    }
//
//    @Test
//     void fetchZipTest() throws Exception {
//        String uri = "https://github.com/halo-dev/halo-theme-pinghsu/archive/master.zip";
//        PowerMockito.doNothing().when(themeService, "downloadZipAndUnzip", Mockito.any(String
//        .class), Mockito.any(Path.class));
//        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "add", Mockito.any(Path
//        .class));
//        ThemeProperty themeProperty = themeService.fetch(uri);
//        Assertions.assertNotNull(themeProperty);
//    }
//
//    @Test
//     void fetchBranchesTest() {
//        String uri = "https://github.com/halo-dev/halo-theme-hux";
//
//        List<ThemeProperty> themeProperties = themeService.fetchBranches(uri);
//
//        Assertions.assertNotNull(themeProperties);
//        Assertions.assertEquals(themeProperties.size(), 2);
//    }
//
//
//    @Test
//     void fetchBranchTest() throws Exception {
//        String uri = "https://github.com/halo-dev/halo-theme-casper";
//        String branch = "master";
//        PowerMockito.doNothing().when(themeService, "downloadZipAndUnzip", Mockito.any(String
//        .class), Mockito.any(Path.class));
//        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "add", Mockito.any(Path
//        .class));
//        ThemeProperty themeProperty = themeService.fetchBranch(uri, branch);
//        Assertions.assertNotNull(themeProperty);
//    }
//
//    @Test
//     void fetchLatestReleaseTest() throws Exception {
//        String uri = "https://github.com/halo-dev/halo-theme-casper";
//        PowerMockito.doNothing().when(themeService, "downloadZipAndUnzip", Mockito.any(String
//        .class), Mockito.any(Path.class));
//        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "add", Mockito.any(Path
//        .class));
//        ThemeProperty themeProperty = themeService.fetchLatestRelease(uri);
//        Assertions.assertNotNull(themeProperty);
//    }
//
//    @Test
//     void updateTest() throws Exception {
//        PowerMockito.doNothing().when(themeService, "downloadZipAndUnzip", Mockito.any(String
//        .class), Mockito.any(Path.class));
//        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "add", Mockito.any(Path
//        .class));
//        PowerMockito.doNothing().when(themeService, "pullFromGit", Mockito.any(ThemeProperty
//        .class));
//        PowerMockito.doReturn(new ThemeProperty()).when(themeService, "getThemeOfNonNullBy",
//        Mockito.any(String.class));
//        ThemeProperty themeProperty = themeService.update("String");
//        Assertions.assertNotNull(themeProperty);
//    }
}
