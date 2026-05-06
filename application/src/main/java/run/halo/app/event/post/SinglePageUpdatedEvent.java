package run.halo.app.event.post;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.core.extension.content.SinglePage;

public class SinglePageUpdatedEvent extends ApplicationEvent {

    @Getter
    private final SinglePage singlePage;

    public SinglePageUpdatedEvent(Object source, SinglePage singlePage) {
        super(source);
        this.singlePage = singlePage;
    }

}
