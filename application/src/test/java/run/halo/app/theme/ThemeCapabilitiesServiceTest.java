package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafProperties;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting.ThemeRouteRules;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.plugin.UiPluginBundleService;
import run.halo.app.plugin.UiPluginResources;

class ThemeCapabilitiesServiceTest {
    @TempDir
    Path root;

    ThemeCapabilitiesService service;
    SystemConfigFetcher configFetcher;
    UiPluginBundleService uiBundleService;
    Theme theme;
    Path templates;

    @BeforeEach
    void setUp() throws IOException {
        var getter = mock(ThemeRootGetter.class);
        when(getter.get()).thenReturn(root);
        configFetcher = mock(SystemConfigFetcher.class);
        uiBundleService = mock(UiPluginBundleService.class);
        service = new ThemeCapabilitiesService(
                getter,
                new ThymeleafProperties(),
                new ThemeLayoutCompatibilityChecker(),
                uiBundleService,
                configFetcher);
        theme = new Theme();
        theme.setMetadata(new Metadata());
        theme.getMetadata().setName("sample");
        theme.setSpec(new Theme.ThemeSpec());
        templates = Files.createDirectories(root.resolve("sample/templates"));
    }

    @Test
    void shouldCombineActualFilesAndDeclarationsWithoutDuplicatingPaths() throws IOException {
        Files.writeString(templates.resolve("post.html"), "post");
        Files.writeString(templates.resolve("moments.html"), "plugin page");
        Files.writeString(templates.resolve("signup.html"), "signup page");
        Files.createDirectories(templates.resolve("modules"));
        Files.writeString(templates.resolve("modules/header.html"), "fragment");
        Files.createDirectories(templates.resolve("error"));
        Files.writeString(templates.resolve("error/404.html"), "not found");
        var custom = new Theme.CustomTemplates();
        custom.setPost(List.of(declaration("post"), declaration("./missing.html"), declaration("error/404")));
        theme.getSpec().setCustomTemplates(custom);

        var result = service.inspectTemplates(theme);

        assertThat(result.templates())
                .filteredOn(file -> file.path().equals("signup.html"))
                .singleElement()
                .satisfies(file -> assertThat(file.usages().getFirst().type()).isEqualTo("system"));
        assertThat(result.templates())
                .filteredOn(file -> file.path().equals("post.html"))
                .singleElement()
                .satisfies(file -> {
                    assertThat(file.usages())
                            .extracting(ThemeCapabilities.ThemeTemplateUsage::type)
                            .containsExactly("system", "custom");
                });
        assertThat(result.templates())
                .extracting(ThemeCapabilities.ThemeTemplateFile::path)
                .doesNotContain("missing.html", "index.html", "layout.html");
        assertThat(result.templates())
                .filteredOn(file -> file.path().equals("moments.html"))
                .singleElement()
                .satisfies(file -> assertThat(file.usages().getFirst().type()).isEqualTo("other"));
        assertThat(result.templates())
                .filteredOn(file -> file.path().equals("error/404.html"))
                .singleElement()
                .satisfies(file -> assertThat(file.usages())
                        .extracting(ThemeCapabilities.ThemeTemplateUsage::type)
                        .containsExactly("custom", "system"));
    }

    @Test
    void shouldRejectEscapingDeclarationsAndSymlinksWithoutReadingTheirContent() throws IOException {
        var outside = Files.writeString(root.resolve("outside.html"), "th:fragment=\"html(head, content)\"");
        Files.createSymbolicLink(templates.resolve("layout.html"), outside);
        var custom = new Theme.CustomTemplates();
        custom.setPage(List.of(declaration("../../outside.html"), declaration(outside.toString())));
        theme.getSpec().setCustomTemplates(custom);

        var result = service.inspectTemplates(theme);

        assertThat(result.pageLayout().getState()).isEqualTo(Theme.PageLayoutState.INVALID);
        assertThat(result.templates()).isEmpty();
    }

    @Test
    void shouldScanAllTemplatesWithoutFollowingLinkedDirectories() throws IOException {
        Files.createSymbolicLink(templates.resolve("linked"), root);
        for (int i = 0; i < 2100; i++) {
            Files.createFile(templates.resolve(i + ".html"));
        }
        assertThat(service.inspectTemplates(theme).templates()).hasSize(2100);
    }

    @Test
    void shouldSkipStaticAssets() throws IOException {
        var assets = Files.createDirectories(templates.resolve("assets"));
        Files.writeString(assets.resolve("qrcode-share.html"), "static page");
        for (int i = 0; i < 2100; i++) {
            Files.createFile(assets.resolve(i + ".html"));
        }
        Files.writeString(templates.resolve("moments.html"), "plugin page");

        var result = service.inspectTemplates(theme);

        assertThat(result.templates())
                .extracting(ThemeCapabilities.ThemeTemplateFile::path)
                .contains("moments.html")
                .noneMatch(path -> path.startsWith("assets/"));
    }

    @Test
    void shouldSkipLinkedStaticAssets() throws IOException {
        Files.createSymbolicLink(templates.resolve("assets"), root);

        assertThat(service.inspectTemplates(theme).templates()).isEmpty();
    }

    @Test
    void shouldCheckLayoutAgainAfterFileChanges() throws IOException {
        assertThat(service.inspectTemplates(theme).pageLayout().getState()).isEqualTo(Theme.PageLayoutState.MISSING);
        Files.writeString(templates.resolve("layout.html"), "<html th:fragment=\"html(head, content)\"></html>");
        assertThat(service.inspectTemplates(theme).pageLayout().getState()).isEqualTo(Theme.PageLayoutState.SUPPORTED);
        Files.writeString(templates.resolve("layout.html"), "<html></html>");
        assertThat(service.inspectTemplates(theme).pageLayout().getState()).isEqualTo(Theme.PageLayoutState.INVALID);
    }

    @Test
    void shouldProvideCurrentNormalizedRoutePatterns() {
        when(uiBundleService.getThemeUiResources(theme))
                .thenReturn(Mono.just(new UiPluginResources("none", null, null, null)));
        var rules = ThemeRouteRules.empty();
        when(configFetcher.fetchRouteRules()).thenReturn(Mono.just(rules));

        StepVerifier.create(service.inspect(theme))
                .assertNext(result -> assertThat(result.routes())
                        .containsEntry("archives.html", "/archives")
                        .containsEntry("category.html", "/categories/{slug}")
                        .containsEntry("tag.html", "/tags/{slug}")
                        .containsEntry("post.html", "/archives/{slug}")
                        .containsEntry("author.html", "/authors/{name}")
                        .containsEntry("page.html", "/{slug}"))
                .verifyComplete();

        rules.setArchives(" journal/ ");
        rules.setCategories(" topics/ ");
        rules.setTags(" labels/ ");
        rules.setPost("/categories/{categorySlug}/{slug}");
        StepVerifier.create(service.inspect(theme))
                .assertNext(result -> assertThat(result.routes())
                        .containsEntry("archives.html", "/journal")
                        .containsEntry("category.html", "/topics/{slug}")
                        .containsEntry("tag.html", "/labels/{slug}")
                        .containsEntry("post.html", "/topics/{categorySlug}/{slug}"))
                .verifyComplete();

        rules.setPost("/archives/{name}");
        StepVerifier.create(service.inspect(theme))
                .assertNext(result -> assertThat(result.routes()).containsEntry("post.html", "/journal/{name}"))
                .verifyComplete();
    }

    private static Theme.TemplateDescriptor declaration(String file) {
        var descriptor = new Theme.TemplateDescriptor();
        descriptor.setName("Custom");
        descriptor.setFile(file);
        return descriptor;
    }
}
