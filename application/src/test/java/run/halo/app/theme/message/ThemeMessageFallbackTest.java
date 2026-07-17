package run.halo.app.theme.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.DefaultResourceLoader;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import run.halo.app.theme.ThemeContext;
import run.halo.app.theme.engine.HaloTemplateEngine;

class ThemeMessageFallbackTest {

    @Test
    void shouldFallBackToHaloMessagesWhenThemeOnlyOverridesTemplate(@TempDir Path themePath) throws Exception {
        var templatesPath = Files.createDirectories(themePath.resolve("templates"));
        Files.writeString(templatesPath.resolve("message-fallback.html"), "<p th:text=\"#{core.only}\"></p>");

        var engine = templateEngine(themePath);

        assertThat(engine.process("message-fallback", new Context(Locale.ENGLISH)))
                .isEqualTo("<p>Halo fallback</p>");
    }

    @Test
    void shouldMergeMessagesUsingSourcePrecedence(@TempDir Path themePath) throws Exception {
        var templatesPath = Files.createDirectories(themePath.resolve("templates"));
        Files.writeString(templatesPath.resolve("message-fallback.html"), """
                <div>
                  <span th:text="#{core.only}"></span>
                  <span th:text="#{shared}"></span>
                  <span th:text="#{global.conflict}"></span>
                  <span th:text="#{locale.priority}"></span>
                </div>
                """);
        Files.writeString(templatesPath.resolve("message-fallback.properties"), """
                shared=Theme template
                locale.priority=Theme default
                """);
        var i18nPath = Files.createDirectories(themePath.resolve("i18n"));
        Files.writeString(i18nPath.resolve("default.properties"), """
                shared=Theme global
                global.conflict=Theme global
                """);

        var engine = templateEngine(themePath);

        assertThat(engine.process("message-fallback", new Context(Locale.ENGLISH)))
                .contains("<span>Halo fallback</span>")
                .contains("<span>Theme template</span>")
                .contains("<span>Halo template</span>")
                .contains("<span>Theme default</span>");
    }

    @Test
    void shouldFallBackForNestedThemeFragment(@TempDir Path themePath) throws Exception {
        var fragmentPath =
                Files.createDirectories(themePath.resolve("templates").resolve("fragments"));
        Files.writeString(
                fragmentPath.resolve("message.html"),
                "<span th:fragment=\"content\" th:text=\"#{fragment.message}\"></span>");

        var engine = templateEngine(themePath);

        assertThat(engine.process("message-fallback-container", new Context(Locale.ENGLISH)))
                .contains("<span>Halo fragment</span>");
    }

    @Test
    void shouldKeepAbsentMessageWhenNoSourceDefinesKey(@TempDir Path themePath) throws Exception {
        var templatesPath = Files.createDirectories(themePath.resolve("templates"));
        Files.writeString(templatesPath.resolve("message-fallback.html"), "<p th:text=\"#{missing.message}\"></p>");

        var engine = templateEngine(themePath);

        assertThat(engine.process("message-fallback", new Context(Locale.ENGLISH)))
                .isEqualTo("<p>??missing.message_en??</p>");
    }

    @Test
    void shouldRequireCorrespondingHaloTemplate(@TempDir Path themePath) throws Exception {
        var templatesPath = Files.createDirectories(themePath.resolve("templates"));
        Files.writeString(templatesPath.resolve("properties-only.html"), "<p th:text=\"#{properties.only}\"></p>");

        var engine = templateEngine(themePath);

        assertThat(engine.process("properties-only", new Context(Locale.ENGLISH)))
                .isEqualTo("<p>??properties.only_en??</p>");
    }

    @Test
    void shouldNotFallBackForFileOutsideActiveTheme(@TempDir Path temporaryPath) throws Exception {
        var themePath = Files.createDirectories(temporaryPath.resolve("theme"));
        var externalTemplatesPath = Files.createDirectories(temporaryPath.resolve("external-templates"));
        Files.writeString(externalTemplatesPath.resolve("message-fallback.html"), "<p th:text=\"#{core.only}\"></p>");

        var engine = templateEngine(themePath, externalTemplatesPath);

        assertThat(engine.process("message-fallback", new Context(Locale.ENGLISH)))
                .isEqualTo("<p>??core.only_en??</p>");
    }

    @Test
    void shouldPropagateHaloMessageParsingFailure(@TempDir Path themePath) throws Exception {
        var templatesPath = Files.createDirectories(themePath.resolve("templates"));
        Files.writeString(templatesPath.resolve("invalid-message.html"), "<p th:text=\"#{invalid.message}\"></p>");

        var engine = templateEngine(themePath);

        assertThatThrownBy(() -> engine.process("invalid-message", new Context(Locale.ENGLISH)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldUseConfiguredHaloTemplateSettings(@TempDir Path themePath) throws Exception {
        var templatesPath = Files.createDirectories(themePath.resolve("templates"));
        Files.writeString(templatesPath.resolve("configured-message.tpl"), "<p th:text=\"#{configured.message}\"></p>");

        var engine = templateEngine(
                themePath, null, "classpath:/configured-templates/", "configured-templates/", ".tpl", "UTF-8");

        assertThat(engine.process("configured-message", new Context(Locale.ENGLISH)))
                .isEqualTo("<p>Configured fallback</p>");
    }

    private static HaloTemplateEngine templateEngine(Path themePath) {
        return templateEngine(themePath, null, "classpath:/templates/", "templates/", ".html", "UTF-8");
    }

    private static HaloTemplateEngine templateEngine(Path themePath, Path externalTemplatesPath) {
        return templateEngine(
                themePath, externalTemplatesPath, "classpath:/templates/", "templates/", ".html", "UTF-8");
    }

    private static HaloTemplateEngine templateEngine(
            Path themePath,
            Path externalTemplatesPath,
            String haloResourcePrefix,
            String haloResolverPrefix,
            String templateSuffix,
            String characterEncoding) {
        var themeContext = ThemeContext.builder()
                .name("test-theme")
                .path(themePath)
                .active(true)
                .build();
        var engine = new HaloTemplateEngine(new ThemeMessageResolver(
                themeContext,
                new DefaultResourceLoader(ThemeMessageFallbackTest.class.getClassLoader()),
                haloResourcePrefix,
                templateSuffix,
                characterEncoding));

        var themeResolver = new FileTemplateResolver();
        themeResolver.setPrefix(themePath.resolve("templates") + "/");
        themeResolver.setSuffix(templateSuffix);
        themeResolver.setCheckExistence(true);
        themeResolver.setCacheable(false);
        themeResolver.setOrder(externalTemplatesPath == null ? 0 : 1);
        engine.addTemplateResolver(themeResolver);

        if (externalTemplatesPath != null) {
            var externalResolver = new FileTemplateResolver();
            externalResolver.setPrefix(externalTemplatesPath + "/");
            externalResolver.setSuffix(templateSuffix);
            externalResolver.setCheckExistence(true);
            externalResolver.setCacheable(false);
            externalResolver.setOrder(0);
            engine.addTemplateResolver(externalResolver);
        }

        var haloResolver = new ClassLoaderTemplateResolver();
        haloResolver.setPrefix(haloResolverPrefix);
        haloResolver.setSuffix(templateSuffix);
        haloResolver.setCheckExistence(true);
        haloResolver.setCacheable(false);
        haloResolver.setOrder(2);
        engine.addTemplateResolver(haloResolver);

        return engine;
    }
}
