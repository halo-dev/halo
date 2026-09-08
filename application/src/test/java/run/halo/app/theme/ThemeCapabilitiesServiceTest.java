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
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.plugin.UiPluginBundleService;

class ThemeCapabilitiesServiceTest {
    @TempDir
    Path root;

    ThemeCapabilitiesService service;
    Theme theme;
    Path templates;

    @BeforeEach
    void setUp() throws IOException {
        var getter = mock(ThemeRootGetter.class);
        when(getter.get()).thenReturn(root);
        service = new ThemeCapabilitiesService(
                getter,
                new ThymeleafProperties(),
                new ThemeLayoutCompatibilityChecker(),
                mock(UiPluginBundleService.class));
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

        assertThat(result.complete()).isTrue();
        assertThat(result.templates())
                .filteredOn(file -> file.path().equals("signup.html"))
                .singleElement()
                .satisfies(file -> assertThat(file.usages().getFirst().type()).isEqualTo("system"));
        assertThat(result.templates())
                .filteredOn(file -> file.path().equals("post.html"))
                .singleElement()
                .satisfies(file -> {
                    assertThat(file.state()).isEqualTo("available");
                    assertThat(file.usages())
                            .extracting(ThemeCapabilities.ThemeTemplateUsage::type)
                            .containsExactly("system", "custom");
                });
        assertThat(result.templates())
                .filteredOn(file -> file.path().equals("missing.html"))
                .singleElement()
                .extracting(ThemeCapabilities.ThemeTemplateFile::state)
                .isEqualTo("missing");
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
        assertThat(result.templates())
                .filteredOn(file -> file.usages().stream()
                        .anyMatch(usage ->
                                usage.type().equals("custom") || usage.type().equals("layout")))
                .allSatisfy(file -> assertThat(file.state()).isEqualTo("invalid"));
    }

    @Test
    void shouldReportIncompleteScanForLinkedDirectoriesAndEntryLimit() throws IOException {
        Files.createSymbolicLink(templates.resolve("linked"), root);
        assertThat(service.inspectTemplates(theme).complete()).isFalse();
        Files.delete(templates.resolve("linked"));
        for (int i = 0; i < ThemeCapabilitiesService.MAX_ENTRIES; i++) {
            Files.createFile(templates.resolve(i + ".html"));
        }
        assertThat(service.inspectTemplates(theme).complete()).isFalse();
    }

    @Test
    void shouldCheckLayoutAgainAfterFileChanges() throws IOException {
        assertThat(service.inspectTemplates(theme).pageLayout().getState()).isEqualTo(Theme.PageLayoutState.MISSING);
        Files.writeString(templates.resolve("layout.html"), "<html th:fragment=\"html(head, content)\"></html>");
        assertThat(service.inspectTemplates(theme).pageLayout().getState()).isEqualTo(Theme.PageLayoutState.SUPPORTED);
        Files.writeString(templates.resolve("layout.html"), "<html></html>");
        assertThat(service.inspectTemplates(theme).pageLayout().getState()).isEqualTo(Theme.PageLayoutState.INVALID);
    }

    private static Theme.TemplateDescriptor declaration(String file) {
        var descriptor = new Theme.TemplateDescriptor();
        descriptor.setName("Custom");
        descriptor.setFile(file);
        return descriptor;
    }
}
