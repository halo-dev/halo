package run.halo.app.migration;

import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MigrationService {

    Mono<Void> backup(Backup backup);

    Mono<Resource> download(Backup backup);

    /**
     * Restores a backup from the given content.
     *
     * @param content backup content
     * @param password password of the encrypted backup, or null for unencrypted backups
     * @return void publisher
     */
    Mono<Void> restore(Publisher<DataBuffer> content, @Nullable String password);

    /**
     * Clean up backup file.
     *
     * @param backup backup detail.
     * @return void publisher.
     */
    Mono<Void> cleanup(Backup backup);

    /**
     * Gets backup files.
     *
     * @return backup files, sorted by last modified time.
     */
    Flux<BackupFile> getBackupFiles();

    /**
     * Get backup file by filename.
     *
     * @param filename filename of backup file
     * @return backup file or empty if file is not found
     */
    Mono<BackupFile> getBackupFile(String filename);
}
