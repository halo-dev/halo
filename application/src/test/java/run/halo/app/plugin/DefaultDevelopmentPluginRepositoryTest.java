package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginRepository;

/**
 * Tests for {@link DefaultDevelopmentPluginRepository}.
 *
 * @author guqing
 * @since 2.7.0
 */
class DefaultDevelopmentPluginRepositoryTest {

    private PluginRepository developmentPluginRepository;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        this.tempDir = Files.createTempDirectory("halo-plugin-repo");
        this.developmentPluginRepository =
            new DefaultDevelopmentPluginRepository();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.delete(tempDir);
    }

    @Test
    void deletePluginPath() {
        boolean deleted = developmentPluginRepository.deletePluginPath(null);
        assertThat(deleted).isTrue();

        // deletePluginPath is a no-op
        deleted = developmentPluginRepository.deletePluginPath(tempDir);
        assertThat(deleted).isTrue();
        assertThat(Files.exists(tempDir)).isTrue();
    }
}
