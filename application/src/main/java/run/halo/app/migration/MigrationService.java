package run.halo.app.migration;

import org.reactivestreams.Publisher;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Mono;

public interface MigrationService {

    Mono<Void> backup(Backup backup);

    Mono<Resource> download(Backup backup);

    Mono<Void> restore(Publisher<DataBuffer> content);

    /**
     * Clean up backup file.
     *
     * @param backup backup detail.
     * @return void publisher.
     */
    Mono<Void> cleanup(Backup backup);

}
