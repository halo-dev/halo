package run.halo.app.security.preauth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import org.junit.jupiter.api.Test;

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
}