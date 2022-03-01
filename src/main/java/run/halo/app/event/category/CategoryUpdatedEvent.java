package run.halo.app.event.category;

import org.springframework.context.ApplicationEvent;
import run.halo.app.model.entity.Category;

/**
 * Category updated event.
 *
 * @author guqing
 * @date 2022-02-24
 */
public class CategoryUpdatedEvent extends ApplicationEvent {

    private final Category category;

    public CategoryUpdatedEvent(Object source, Category category) {
        super(source);
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }
}
