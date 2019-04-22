package run.halo.app.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.annotation.PreDestroy;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Event queue dispatcher.
 *
 * @author johnniang
 * @date 19-4-20
 */
@Slf4j
@Deprecated
public class ApplicationEventQueuePublisher {

    private final BlockingQueue<Object> events = new LinkedBlockingQueue<>();

    private final ApplicationListenerManager listenerManager;

    private final ExecutorService executorService;

    public ApplicationEventQueuePublisher(ApplicationListenerManager listenerManager) {
        this.listenerManager = listenerManager;
        this.executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new EventQueueConsumer());
    }

    public void publishEvent(Object event) {
        try {
            events.put(event);
        } catch (InterruptedException e) {
            log.warn("Failed to put event to the queue", e);
            // Ignore this error
        }
    }

    @PreDestroy
    protected void destroy() {
        log.info("Shutting down all event queue consumer");
        this.executorService.shutdownNow();
    }

    @SuppressWarnings("unchecked")
    private class EventQueueConsumer implements Runnable {

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Take an event
                    Object event = events.take();

                    // Get listeners
                    List<EventListener> listeners = listenerManager.getListeners(event);

                    // Handle the event
                    listeners.forEach(listener -> {
                        if (listener instanceof ApplicationListener && event instanceof ApplicationEvent) {
                            ApplicationEvent applicationEvent = (ApplicationEvent) event;
                            // Fire event
                            ((ApplicationListener) listener).onApplicationEvent(applicationEvent);
                        }
                    });

                    log.info("Event queue consumer has been shut down");
                } catch (InterruptedException e) {
                    log.warn("Failed to take event", e);
                } catch (Exception e) {
                    log.error("Failed to handle event", e);
                }
            }
        }
    }
}
