package run.halo.app.event.post;

import run.halo.app.core.extension.Counter;

public class CounterUpdatedEvent extends CounterEvent {
    public CounterUpdatedEvent(Object source, Counter counter) {
        super(source, counter);
    }
}
