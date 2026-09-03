package run.halo.app.security.preauth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

/**
 * Tests for {@link SystemSetupEndpoint}.
 *
 * @author guqing
 * @since 2.20.0
 */
class SystemSetupEndpointTest {

    @Test
    void placeholderTest() {
        var properties = new Properties();
        properties.setProperty("username", "guqing");
        properties.setProperty("timestamp", "2024-09-30");
        var str = SystemSetupEndpoint.PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders("""
            ${username}
            ${timestamp}
            """, properties);
        assertThat(str).isEqualTo("""
            guqing
            2024-09-30
            """);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldBindDefaultArchivesMenuItemToThemeRoute() {
        var extensions = new YamlUnstructuredLoader(new ClassPathResource("initial-data.yaml")).load();
        var archives = extensions.stream()
                .filter(extension -> "MenuItem".equals(extension.getKind()))
                .filter(extension -> "c4c814d1-0c2c-456b-8c96-4864965fee94"
                        .equals(extension.getMetadata().getName()))
                .findFirst()
                .orElseThrow();
        var spec = (Map<String, Object>) archives.getData().get("spec");

        assertThat(spec).containsEntry("routeRef", "archives").doesNotContainKey("href");
    }
}
