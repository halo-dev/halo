package run.halo.app.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.FileSystemUtils;
import run.halo.app.infra.properties.HaloProperties;

/**
 * Tests for {@link VisitLogWriter}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class VisitLogWriterTest {

    @Mock
    private HaloProperties haloProperties;

    private VisitLogWriter visitLogWriter;

    private Path workDir;

    @BeforeEach
    void setUp() throws IOException {
        workDir = Files.createTempDirectory("halo-visitlog");
        when(haloProperties.getWorkDir()).thenReturn(workDir);
        visitLogWriter = new VisitLogWriter(haloProperties);
    }

    @AfterEach
    void tearDown() throws Exception {
        visitLogWriter.destroy();
        FileSystemUtils.deleteRecursively(workDir);
    }

    @Test
    void start() {
        assertThat(visitLogWriter.isStarted()).isFalse();
        visitLogWriter.start();
        assertThat(visitLogWriter.isStarted()).isTrue();
    }

    @Test
    void log() throws IOException, InterruptedException {
        visitLogWriter.start();
        Path logPath = workDir.resolve(visitLogWriter.getLogFilePath());
        List<String> logs = Files.readAllLines(logPath);
        assertThat(logs.size()).isEqualTo(0);

        for (int i = 0; i < 5; i++) {
            visitLogWriter.log("log-" + i);
        }

        logs = Files.readAllLines(logPath);
        assertThat(logs.size()).isEqualTo(0);

        for (int i = 5; i < 10; i++) {
            visitLogWriter.log("log-" + i);
        }
        // wait for flush
        Thread.sleep(1000);

        logs = Files.readAllLines(logPath);
        assertThat(logs.size()).isEqualTo(10);
    }
}