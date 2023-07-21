package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.PluginRepository;

/**
 * Tests for {@link DefaultDevelopmentPluginRepository}.
 *
 * @author guqing
 * @since 2.8.0
 */
class DefaultDevelopmentPluginRepositoryTest {

    private PluginRepository developmentPluginRepository;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        this.developmentPluginRepository =
            new DefaultDevelopmentPluginRepository();
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
