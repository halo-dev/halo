package run.halo.app.event.post;

import org.springframework.context.ApplicationEvent;
import run.halo.app.core.extension.Counter;

public abstract class CounterEvent extends ApplicationEvent {

    private final Counter counter;

    public CounterEvent(Object source, Counter counter) {
        super(source);
        this.counter = counter;
    }

    public Counter getCounter() {
        return counter;
    }
}
