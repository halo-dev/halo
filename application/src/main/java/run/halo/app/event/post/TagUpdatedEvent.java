package run.halo.app.event.post;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.core.extension.content.Tag;

public class TagUpdatedEvent extends ApplicationEvent {

    @Getter private final Tag tag;

    public TagUpdatedEvent(Object source, Tag tag) {
        super(source);
        this.tag = tag;
    }
}
