package run.halo.app.extension.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.springframework.util.Assert;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.WatcherPredicates;
import run.halo.app.extension.controller.Reconciler.Request;

public class ControllerBuilder {

    private final String name;

    private Duration minDelay;

    private Duration maxDelay;

    private Reconciler<Request> reconciler;

    private Supplier<Instant> nowSupplier;

    private Extension extension;

    private Predicate<Extension> onAddPredicate;

    private Predicate<Extension> onDeletePredicate;

    private BiPredicate<Extension, Extension> onUpdatePredicate;

    private final ExtensionClient client;

    private boolean syncAllOnStart = true;

    private int workerCount = 1;

    public ControllerBuilder(Reconciler<Request> reconciler, ExtensionClient client) {
        Assert.notNull(reconciler, "Reconciler must not be null");
        Assert.notNull(client, "Extension client must not be null");
        this.name = reconciler.getClass().getName();
        this.reconciler = reconciler;
        this.client = client;
    }

    public ControllerBuilder minDelay(Duration minDelay) {
        this.minDelay = minDelay;
        return this;
    }

    public ControllerBuilder maxDelay(Duration maxDelay) {
        this.maxDelay = maxDelay;
        return this;
    }

    public ControllerBuilder nowSupplier(Supplier<Instant> nowSupplier) {
        this.nowSupplier = nowSupplier;
        return this;
    }

    public ControllerBuilder extension(Extension extension) {
        this.extension = extension;
        return this;
    }

    public ControllerBuilder onAddPredicate(Predicate<Extension> onAddPredicate) {
        this.onAddPredicate = onAddPredicate;
        return this;
    }

    public ControllerBuilder onDeletePredicate(Predicate<Extension> onDeletePredicate) {
        this.onDeletePredicate = onDeletePredicate;
        return this;
    }

    public ControllerBuilder onUpdatePredicate(
        BiPredicate<Extension, Extension> onUpdatePredicate) {
        this.onUpdatePredicate = onUpdatePredicate;
        return this;
    }

    public ControllerBuilder syncAllOnStart(boolean syncAllAtStart) {
        this.syncAllOnStart = syncAllAtStart;
        return this;
    }

    public ControllerBuilder workerCount(int workerCount) {
        this.workerCount = workerCount;
        return this;
    }

    public Controller build() {
        if (nowSupplier == null) {
            nowSupplier = Instant::now;
        }
        if (minDelay == null || minDelay.isNegative() || minDelay.isZero()) {
            minDelay = Duration.ofMillis(5);
        }
        if (maxDelay == null || maxDelay.isNegative() || maxDelay.isZero()) {
            maxDelay = Duration.ofSeconds(1000);
        }
        Assert.isTrue(minDelay.compareTo(maxDelay) <= 0,
            "Min delay must be less than or equal to max delay");
        Assert.notNull(extension, "Extension must not be null");
        Assert.notNull(reconciler, "Reconciler must not be null");

        var queue = new DefaultQueue<Request>(nowSupplier, minDelay);
        var predicates = new WatcherPredicates.Builder()
            .withGroupVersionKind(extension.groupVersionKind())
            .onAddPredicate(onAddPredicate)
            .onUpdatePredicate(onUpdatePredicate)
            .onDeletePredicate(onDeletePredicate)
            .build();
        var watcher = new ExtensionWatcher(queue, predicates);
        var synchronizer = new RequestSynchronizer(syncAllOnStart,
            client,
            extension,
            watcher,
            predicates.onAddPredicate());
        return new DefaultController<>(name, reconciler, queue, synchronizer, minDelay, maxDelay,
            workerCount);
    }
}
