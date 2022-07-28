package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;
import org.thymeleaf.templateresource.ITemplateResource;
import run.halo.app.infra.NotFoundException;

/**
 * Tests for {@link ThemeResourceTemplateResolver}.
 *
 * @author guqing
 * @since 2.0.0
 */
class ThemeResourceTemplateResolverTest {
    private ThemeResourceTemplateResolver themeResourceTemplateResolver;
    private URL defaultThemeUrl;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        defaultThemeUrl = ResourceUtils.getURL("classpath:themes/default");
        themeResourceTemplateResolver = new ThemeResourceTemplateResolver();
    }

    @Test
    void computeTemplateResource() throws URISyntaxException {
        populateDefaultContext();
        ITemplateResource templateResource =
            themeResourceTemplateResolver.computeTemplateResource(null, null, "index", "index.html",
                StandardCharsets.UTF_8.name(), null);
        assertThat(templateResource).isNotNull();
    }

    @Test
    void computeTemplateResourceNotExist() throws URISyntaxException {
        populateDefaultContext();
        assertThatThrownBy(() -> {
            themeResourceTemplateResolver.computeTemplateResource(null, null, "post", "post.html",
                StandardCharsets.UTF_8.name(), null);
        }).isInstanceOf(NotFoundException.class)
            .hasMessage("Template [post.html] not found in theme [default]");
    }

    void populateDefaultContext() throws URISyntaxException {
        ThemeContext themeContext = ThemeContext.builder()
            .themeName("default")
            .path(Paths.get(defaultThemeUrl.toURI()))
            .isActive(true)
            .build();
        ThemeContextHolder.setThemeContext(themeContext);
    }
}