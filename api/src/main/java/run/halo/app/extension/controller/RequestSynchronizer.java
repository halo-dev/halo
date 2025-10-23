package run.halo.app.extension.controller;

import static org.springframework.data.domain.Sort.Direction.ASC;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Watcher;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.extension.index.query.Queries;

@Slf4j
public class RequestSynchronizer implements Synchronizer<Request> {

    /**
     * Default batch size for listing Extension names.
     */
    private static final Integer DEFAULT_BATCH_SIZE = 100;

    private final ExtensionClient client;

    private final Class<? extends Extension> type;

    private final boolean syncAllOnStart;

    private volatile boolean disposed = false;

    private final Watcher watcher;

    private final ListOptions listOptions;

    @Getter
    private volatile boolean started = false;

    public RequestSynchronizer(boolean syncAllOnStart,
        ExtensionClient client,
        Extension extension,
        Watcher watcher,
        ListOptions listOptions) {
        this.syncAllOnStart = syncAllOnStart;
        this.client = client;
        this.type = extension.getClass();
        this.watcher = watcher;
        this.listOptions = listOptions;
    }

    @Override
    public void start() {
        if (isDisposed() || started) {
            return;
        }
        log.info("Starting request({}) synchronizer...", type);
        started = true;

        if (syncAllOnStart) {
            // list all in batch
            int batchSize = DEFAULT_BATCH_SIZE;
            var sort = Sort.by(ASC, "metadata.name");
            // get the first batch to determine the current name
            var names = client.listTopNames(type, listOptions, sort, batchSize);
            names.forEach(name -> watcher.onAdd(new Request(name)));
            while (names.size() == batchSize) {
                var lastName = names.getLast();
                var augmentedOptions = ListOptions.builder(listOptions)
                    .andQuery(Queries.greaterThan("metadata.name", lastName))
                    .build();
                names = client.listTopNames(type, augmentedOptions, sort, batchSize);
                names.forEach(name -> watcher.onAdd(new Request(name)));
            }
        }
        client.watch(this.watcher);
        log.info("Started request({}) synchronizer.", type);
    }

    @Override
    public void dispose() {
        disposed = true;
        watcher.dispose();
    }

    @Override
    public boolean isDisposed() {
        return this.disposed;
    }
}
