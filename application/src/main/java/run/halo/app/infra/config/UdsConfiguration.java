package run.halo.app.infra.config;

import io.netty.channel.unix.DomainSocketAddress;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/**
 * Configuration for Unix Domain Socket (UDS) support.
 *
 * <p>Creates an additional Netty {@link HttpServer} bound to a Unix Domain Socket
 * path when UDS is enabled. This server runs alongside the main TCP listener
 * without interfering with it.</p>
 *
 * @author drin-love
 */
@Slf4j
@Configuration
public class UdsConfiguration {

    @Bean(destroyMethod = "dispose")
    @ConditionalOnProperty(prefix = "halo.uds", name = "enabled", havingValue = "true")
    DisposableServer udsDisposableServer(
        HttpHandler httpHandler,
        run.halo.app.infra.properties.UdsProperties udsProperties
    ) {
        var udsPath = resolveUdsPath(udsProperties);
        cleanupSocketFile(udsPath);

        log.info("Starting Unix Domain Socket server on {}", udsPath);

        var adapter = new ReactorHttpHandlerAdapter(httpHandler);

        var server = HttpServer.create()
            .bindAddress(() -> new DomainSocketAddress(udsPath.toAbsolutePath().toString()))
            .handle(adapter)
            .wiretap(false)
            .bindNow(Duration.ofSeconds(30));

        log.info("Unix Domain Socket server started successfully on {}", udsPath);

        return server;
    }

    private Path resolveUdsPath(run.halo.app.infra.properties.UdsProperties udsProperties) {
        var path = udsProperties.getPath();
        if (path == null) {
            var workDir = System.getProperty("halo.work-dir");
            if (workDir != null) {
                path = Path.of(workDir, "halo.sock");
            } else {
                path = Path.of(System.getProperty("user.home"), ".halo2", "halo.sock");
            }
        }
        return path.toAbsolutePath().normalize();
    }

    private void cleanupSocketFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to clean up existing UDS socket file: {}", path, e);
        }
    }
}
