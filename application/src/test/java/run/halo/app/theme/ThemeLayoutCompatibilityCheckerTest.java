package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import run.halo.app.core.extension.Theme;

class ThemeLayoutCompatibilityCheckerTest {

    @TempDir
    Path tempDirectory;

    final ThemeLayoutCompatibilityChecker checker = new ThemeLayoutCompatibilityChecker();

    @Test
    void shouldSupportLayoutWithThemeSpecificExpressionsAndTemplateReferences() throws IOException {
        Files.createDirectories(tempDirectory.resolve("templates"));
        Files.writeString(tempDirectory.resolve("templates").resolve("layout.html"), """
                <!doctype html>
                <html xmlns:th="https://www.thymeleaf.org" th:fragment="html (head,content)">
                  <head>
                    <title th:text="${site.title}"></title>
                    <th:block th:replace="${head}" />
                  </head>
                  <body th:style="'background-color: '+${theme.config.style.background_color ?: '#f2f2f2'}">
                    <header th:replace="~{modules/header :: header}"></header>
                    <th:block th:replace="${content}" />
                  </body>
                </html>
                """);

        var result = checker.check(tempDirectory);

        assertThat(result.getState()).isEqualTo(Theme.PageLayoutState.SUPPORTED);
        assertThat(result.getReason()).isEqualTo("LayoutTemplateSupported");
    }

    @Test
    void shouldReportInvalidLayoutWhenFragmentSignatureDoesNotMatchContract() throws IOException {
        Files.createDirectories(tempDirectory.resolve("templates"));
        Files.writeString(tempDirectory.resolve("templates").resolve("layout.html"), """
                <!doctype html>
                <html xmlns:th="https://www.thymeleaf.org" th:fragment="html (title, content)">
                  <body><th:block th:replace="${content}" /></body>
                </html>
                """);

        var result = checker.check(tempDirectory);

        assertThat(result.getState()).isEqualTo(Theme.PageLayoutState.INVALID);
        assertThat(result.getReason()).isEqualTo("UnsupportedLayoutFragment");
    }
}
