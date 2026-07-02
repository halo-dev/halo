package run.halo.app.theme.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.dialect.SpringStandardDialect;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;

class PageLayoutTemplateResolverTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldRenderSystemFallbackLayoutWhenThemeLayoutMissing() {
        var engine = templateEngine(tempDirectory, false, null);

        var result = engine.process("plugin:fake-plugin:page", new Context());

        assertThat(result).contains("<title>Plugin page</title>");
        assertThat(result).contains("<main>Plugin content</main>");
        assertThat(result).contains("<halo:footer />");
    }

    @Test
    void shouldRenderWithoutHeadFragmentWhenFallbackLayoutIsUsed() {
        var engine = templateEngine(tempDirectory, false, """
            <!doctype html>
            <html xmlns:th="https://www.thymeleaf.org"
              th:replace="~{layout :: html(head = null, content = ~{::content})}">
              <th:block th:fragment="content">
                <main>Content without head</main>
              </th:block>
            </html>
            """);

        var result = engine.process("plugin:fake-plugin:page", new Context());

        assertThat(result).contains("<main>Content without head</main>");
        assertThat(result).contains("<head>");
    }

    @Test
    void shouldRenderThemeLayoutWhenCompatible() throws IOException {
        Files.createDirectories(tempDirectory.resolve("templates"));
        Files.writeString(tempDirectory.resolve("templates").resolve("layout.html"), """
                <!doctype html>
                <html xmlns:th="https://www.thymeleaf.org" th:fragment="html (head, content)">
                  <head><th:block th:replace="${head}" /></head>
                  <body>
                    <header>Theme header</header>
                    <th:block th:replace="${content}" />
                  </body>
                </html>
                """);
        var engine = templateEngine(tempDirectory, true, null);

        var result = engine.process("plugin:fake-plugin:page", new Context());

        assertThat(result).contains("Theme header");
        assertThat(result).contains("<main>Plugin content</main>");
    }

    @Test
    void shouldNotSelectPluginLocalLayoutForContractLayout() {
        var engine = templateEngine(tempDirectory, false, null);
        engine.addTemplateResolver(new PluginLocalLayoutResolver());

        var result = engine.process("plugin:fake-plugin:page", new Context());

        assertThat(result).contains("<main>Plugin content</main>");
        assertThat(result).doesNotContain("Plugin local layout");
    }

    @Test
    void shouldKeepNonContractPluginRelativeResolutionUnchanged() {
        var engine = templateEngine(tempDirectory, false, """
            <!doctype html>
            <html xmlns:th="https://www.thymeleaf.org" th:replace="~{modules/layout}">
            </html>
            """);
        engine.addTemplateResolver(new PluginLocalLayoutResolver());

        var result = engine.process("plugin:fake-plugin:page", new Context());

        assertThat(result).contains("Plugin local layout");
    }

    private static TemplateEngine templateEngine(Path themePath, boolean themeLayoutSupported, String pageTemplate) {
        var engine = new TemplateEngine();
        engine.setDialect(new SpringStandardDialect());
        var pageLayoutTemplateResolver = new PageLayoutTemplateResolver(themePath, themeLayoutSupported);
        pageLayoutTemplateResolver.setPrefix("classpath:/templates/");
        pageLayoutTemplateResolver.setSuffix(".html");
        pageLayoutTemplateResolver.setTemplateMode("HTML");
        pageLayoutTemplateResolver.setOrder(0);
        engine.addTemplateResolver(pageLayoutTemplateResolver);

        var pluginPageTemplateResolver = new PluginPageTemplateResolver(pageTemplate);
        pluginPageTemplateResolver.setOrder(1);
        engine.addTemplateResolver(pluginPageTemplateResolver);
        return engine;
    }

    private static class PluginPageTemplateResolver extends StringTemplateResolver {
        private final String pageTemplate;

        PluginPageTemplateResolver(String pageTemplate) {
            this.pageTemplate = pageTemplate == null ? """
                    <!doctype html>
                    <html xmlns:th="https://www.thymeleaf.org"
                      th:replace="~{layout :: html(head = ~{::head}, content = ~{::content})}">
                      <th:block th:fragment="head">
                        <title>Plugin page</title>
                      </th:block>
                      <th:block th:fragment="content">
                        <main>Plugin content</main>
                      </th:block>
                    </html>
                    """ : pageTemplate;
        }

        @Override
        protected ITemplateResource computeTemplateResource(
                IEngineConfiguration configuration,
                String ownerTemplate,
                String template,
                Map<String, Object> templateResolutionAttributes) {
            if (!"plugin:fake-plugin:page".equals(template)) {
                return null;
            }
            return new StringTemplateResource(pageTemplate);
        }
    }

    private static class PluginLocalLayoutResolver extends StringTemplateResolver {
        @Override
        protected ITemplateResource computeTemplateResource(
                IEngineConfiguration configuration,
                String ownerTemplate,
                String template,
                Map<String, Object> templateResolutionAttributes) {
            if (!"modules/layout".equals(template) && !"layout".equals(template)) {
                return null;
            }
            return new StringTemplateResource("""
                <section>Plugin local layout</section>
                """);
        }
    }
}
