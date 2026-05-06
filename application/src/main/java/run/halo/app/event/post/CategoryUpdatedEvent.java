package run.halo.app.event.post;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.core.extension.content.Category;

public class CategoryUpdatedEvent extends ApplicationEvent {

    @Getter private final Category category;

    public CategoryUpdatedEvent(Object source, Category category) {
        super(source);
        this.category = category;
    }
}
