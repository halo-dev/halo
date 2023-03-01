package run.halo.app.metrics;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import reactor.core.Disposable;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.FileUtils;

/**
 * Visit log writer.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Deprecated
public class VisitLogWriter implements InitializingBean, DisposableBean {
    private static final String LOG_FILE_NAME = "visits.log";
    private static final String LOG_FILE_LOCATION = "logs";
    private final AsyncLogWriter asyncLogWriter;
    private volatile boolean interruptThread = false;
    private volatile boolean started = false;
    private final ExecutorService executorService;

    private final Path logFilePath;

    public VisitLogWriter(HaloProperties haloProperties) throws IOException {
        Path logsPath = haloProperties.getWorkDir()
            .resolve(LOG_FILE_LOCATION);
        if (!Files.exists(logsPath)) {
            Files.createDirectories(logsPath);
        }
        this.logFilePath = logsPath.resolve(LOG_FILE_NAME);
        this.asyncLogWriter = new AsyncLogWriter(logFilePath);
        this.executorService = Executors.newFixedThreadPool(1);
    }

    public void log(String logMsg) {
        asyncLogWriter.put(logMsg);
    }

    public Path getLogFilePath() {
        return logFilePath;
    }

    void start() {
        if (started) {
            return;
        }
        log.debug("Starting write visit log...");
        this.started = true;
        executorService.submit(() -> {
            while (!interruptThread && !Thread.currentThread().isInterrupted()) {
                try {
                    asyncLogWriter.writeLog();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("VisitLogWrite thread [{}] interrupted",
                        Thread.currentThread().getName());
                }
            }
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public boolean isStarted() {
        return started;
    }

    public long queuedSize() {
        return asyncLogWriter.logQueue.size();
    }

    @Override
    public void destroy() throws Exception {
        this.started = false;
        interruptThread = true;
        asyncLogWriter.dispose();
        executorService.shutdownNow();
    }

    static class AsyncLogWriter implements Disposable {
        private static final int MAX_LOG_SIZE = 10000;
        private static final int BATCH_SIZE = 10;
        private final BufferedOutputStream writer;
        private final BlockingQueue<String> logQueue;
        private final AtomicInteger logBatch = new AtomicInteger(0);
        private volatile boolean disposed = false;

        public AsyncLogWriter(Path logFilePath) {
            OutputStream outputStream;
            try {
                outputStream = Files.newOutputStream(logFilePath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.writer = new BufferedOutputStream(outputStream);
            this.logQueue = new LinkedBlockingDeque<>(MAX_LOG_SIZE);
        }

        public void writeLog() throws InterruptedException {
            String logMessage = logQueue.take();
            if (StringUtils.isBlank(logMessage)) {
                return;
            }
            writeToDisk(logMessage);
            log.debug("Consumption visit log message: [{}]", logMessage);
        }

        void writeToDisk(String logMsg) {
            String format = String.format("%s %s\n", Instant.now(), logMsg);
            try {
                writer.write(format.getBytes(), 0, format.length());
                int size = logBatch.incrementAndGet();
                if (size >= BATCH_SIZE) {
                    writer.flush();
                    logBatch.set(0);
                }
            } catch (IOException e) {
                log.warn("Record access log failure: ", ExceptionUtils.getRootCause(e));
            }
        }

        public void put(String logMessage) {
            // add log message to queue tail
            logQueue.add(logMessage);
            log.debug("Production a log messages [{}]", logMessage);
        }

        @Override
        public void dispose() {
            this.disposed = true;
            if (writer != null) {
                try {
                    writer.flush();
                } catch (IOException e) {
                    // ignore this
                }
                FileUtils.closeQuietly(writer);
            }
        }

        @Override
        public boolean isDisposed() {
            return this.disposed;
        }
    }
}
