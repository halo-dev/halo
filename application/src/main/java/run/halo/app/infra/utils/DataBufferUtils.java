package run.halo.app.infra.utils;

import static org.springframework.core.io.buffer.DataBufferUtils.releaseConsumer;
import static org.springframework.core.io.buffer.DataBufferUtils.write;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

@Slf4j
public enum DataBufferUtils {
    ;

    public static Mono<InputStream> toInputStream(Publisher<DataBuffer> content) {
        return toInputStream(content, Schedulers.boundedElastic());
    }

    public static Mono<InputStream> toInputStream(Publisher<DataBuffer> content,
        Scheduler scheduler) {
        return Mono.create(sink -> {
            try {
                var pos = new PipedOutputStream();
                var pis = new PipedInputStream(pos);
                var disposable = write(content, pos)
                    .subscribeOn(scheduler)
                    .subscribe(releaseConsumer(), sink::error, () -> FileUtils.closeQuietly(pos),
                        Context.of(sink.contextView()));
                sink.onDispose(disposable);
                sink.success(pis);
            } catch (IOException e) {
                sink.error(e);
            }
        });
    }
}
