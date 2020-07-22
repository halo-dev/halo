package run.halo.app.handler.read.impl;

import run.halo.app.handler.read.Read;
import run.halo.app.handler.read.ReadStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: HeHui
 * @date: 2020-07-20 14:44
 * @description: smooth reading interface abstract
 */
public abstract class ReadAbstract<T extends Number, ID> implements Read<T, ID> {


    /**
     * Maximum reading value, when reached
     */
    private final T maxRead;

    /**
     * Timing how many seconds
     */
    protected final int jobSeconds;


    /**
     * maximum or task method is called
     */
    protected final ReadStorage<T, ID> readStorage;


    /**
     * record
     */
    private final AtomicInteger isRead = new AtomicInteger(0);


    /**
     * build
     *
     * @param maxRead     maximum number of readings
     * @param jobSeconds  timing seconds
     * @param readStorage read the stored
     */
    protected ReadAbstract(T maxRead, int jobSeconds, ReadStorage<T, ID> readStorage) {
        assert readStorage != null : "ReadStorage not null";
        assert maxRead != null : "maximum number not null";
        assert maxRead.doubleValue() > 0.01D : "maximum number not less than 0.01D";
        assert jobSeconds > 60 : "timing seconds not less than 60";

        this.maxRead = maxRead;
        this.jobSeconds = jobSeconds;
        this.readStorage = readStorage;

        this.initJob();
    }


    /**
     * init job task
     */
    private void initJob() {
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        scheduledExecutorService.scheduleWithFixedDelay(this::perform, 0, jobSeconds, TimeUnit.SECONDS);
    }


    /**
     * task
     */
    protected void perform() {
        if (isRead.get() > 0) {
            this.getAll().ifPresent(data -> {
                readStorage.increase(data);
                this.clear();
            });
            isRead.getAndSet(0);
        }
    }


    /**
     * reading
     *
     * @param key      reading info unique identifier
     * @param n        increase
     * @param clientID If client restrictions need to be increased (not required)
     */
    @Override
    public void read(ID key, T n, String clientID) {
        //TODO is clientID not null ?
        isRead.incrementAndGet();

        Optional<T> optional = this.increase(key, n);
        /**
         *  The maximum number condition is met
         */
        if (optional.isPresent() && optional.get().doubleValue() >= maxRead.doubleValue()) {
            readStorage.increase(key, optional.get());
            /**
             *  Immediately reduce the corresponding number in the cache
             */
            this.reduce(key, optional.get());
        }
    }


    /**
     * increase
     *
     * @param key reading info unique identifier
     * @param n   increase num
     * @return {@link T} result read after num
     */
    protected abstract Optional<T> increase(ID key, T n);


    /**
     * reduce
     *
     * @param key reading info unique identifier
     * @param n   num
     */
    protected abstract void reduce(ID key, T n);


    /**
     * get all info increase
     *
     * @return {@link Map<ID, T>}
     */
    protected abstract Optional<Map<ID, T>> getAll();


    /**
     * clear all
     */
    protected abstract void clear();
}
