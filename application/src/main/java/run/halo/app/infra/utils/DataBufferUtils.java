package run.halo.app.infra.utils;

import static org.springframework.core.io.buffer.DataBufferUtils.releaseConsumer;
import static org.springframework.core.io.buffer.DataBufferUtils.write;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public final class DataBufferUtils {

    private DataBufferUtils() {
    }

    public static InputStream toInputStream(Flux<DataBuffer> content) throws IOException {
        var pos = new PipedOutputStream();
        var pis = new PipedInputStream(pos);
        write(content, pos)
            .doOnComplete(() -> {
                try {
                    pos.close();
                } catch (IOException ignored) {
                    // Ignore the error
                }
            })
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(releaseConsumer(), error -> {
                if (error instanceof IOException) {
                    // Ignore the error
                    return;
                }
                log.error("Failed to write DataBuffer into OutputStream", error);
            });
        return pis;
    }
}
