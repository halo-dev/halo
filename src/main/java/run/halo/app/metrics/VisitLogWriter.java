package run.halo.app.metrics;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.FileUtils;

/**
 * Visit log writer.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class VisitLogWriter implements InitializingBean, DisposableBean {
    private static final String LOG_FILE_NAME = "visits.log";
    private static final String LOG_FILE_LOCATION = "logs";
    private final AsyncLogWriter asyncLogWriter;
    private volatile boolean interruptThread = false;

    private final Path logFilePath;

    private VisitLogWriter(HaloProperties haloProperties) throws IOException {
        Path logsPath = haloProperties.getWorkDir()
            .resolve(LOG_FILE_LOCATION);
        if (!Files.exists(logsPath)) {
            Files.createDirectories(logsPath);
        }
        this.logFilePath = logsPath.resolve(LOG_FILE_NAME);
        this.asyncLogWriter = new AsyncLogWriter(logFilePath);
    }

    public synchronized void log(String logMsg) {
        asyncLogWriter.put(logMsg);
    }

    public Path getLogFilePath() {
        return logFilePath;
    }

    void start() {
        log.debug("Starting write visit log...");
        Thread thread = new Thread(() -> {
            while (!interruptThread) {
                asyncLogWriter.writeLog();
            }
        }, "visits-log-writer");
        thread.start();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    @Override
    public void destroy() throws Exception {
        asyncLogWriter.close();
        interruptThread = true;
    }

    static class AsyncLogWriter {
        private static final int MAX_LOG_SIZE = 10000;
        private static final int BATCH_SIZE = 10;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition fullCondition = lock.newCondition();
        private final Condition emptyCondition = lock.newCondition();
        private final BufferedOutputStream writer;
        private final Deque<String> logQueue;
        private final AtomicInteger logBatch = new AtomicInteger(0);

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
            this.logQueue = new ArrayDeque<>();
        }

        public void writeLog() {
            lock.lock();
            try {
                // queue is empty, wait for new log
                while (logQueue.isEmpty()) {
                    try {
                        emptyCondition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                String logMessage = logQueue.poll();
                writeToDisk(logMessage);
                log.debug("Consumption visit log message: [{}]", logMessage);
                // signal log producer
                fullCondition.signal();
            } finally {
                lock.unlock();
            }
        }

        void writeToDisk(String logMsg) {
            String format = String.format("%s %s\n", Instant.now(), logMsg);
            lock.lock();
            try {
                writer.write(format.getBytes(), 0, format.length());
                int size = logBatch.incrementAndGet();
                if (size >= BATCH_SIZE) {
                    writer.flush();
                    logBatch.set(0);
                }
            } catch (IOException e) {
                log.warn("Record access log failure: ", ExceptionUtils.getRootCause(e));
            } finally {
                lock.unlock();
            }
        }

        public void put(String logMessage) {
            lock.lock();
            try {
                while (logQueue.size() == MAX_LOG_SIZE) {
                    try {
                        log.debug("Queue full, producer thread waiting...");
                        fullCondition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // add log message to queue tail
                logQueue.add(logMessage);
                log.info("Production a log messages [{}]", logMessage);
                // signal consumer thread
                emptyCondition.signal();
            } finally {
                lock.unlock();
            }
        }

        public void close() {
            if (writer != null) {
                try {
                    writer.flush();
                } catch (IOException e) {
                    // ignore this
                }
                FileUtils.closeQuietly(writer);
            }
        }
    }
}
