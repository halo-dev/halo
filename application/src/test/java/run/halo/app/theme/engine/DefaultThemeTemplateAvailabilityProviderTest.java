package run.halo.app.theme.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.util.ResourceUtils;
import run.halo.app.theme.ThemeContext;

@ExtendWith(MockitoExtension.class)
class DefaultThemeTemplateAvailabilityProviderTest {

    @InjectMocks
    DefaultThemeTemplateAvailabilityProvider provider;

    @Mock
    ThymeleafProperties thymeleafProperties;

    @Test
    void templateAvailableTest() throws FileNotFoundException, URISyntaxException {
        var themeUrl = ResourceUtils.getURL("classpath:themes/default");
        var themePath = Path.of(themeUrl.toURI());

        when(thymeleafProperties.getSuffix()).thenReturn(".html");
        var themeContext = ThemeContext.builder()
            .name("default")
            .path(themePath)
            .build();
        boolean templateAvailable = provider.isTemplateAvailable(themeContext, "fake");
        assertFalse(templateAvailable);

        templateAvailable = provider.isTemplateAvailable(themeContext, "index");
        assertTrue(templateAvailable);

        templateAvailable = provider.isTemplateAvailable(themeContext, "timezone");
        assertTrue(templateAvailable);
    }
}