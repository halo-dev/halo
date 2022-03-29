package run.halo.app.event.category;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.Nullable;
import run.halo.app.model.entity.Category;

/**
 * Category updated event.
 *
 * @author guqing
 * @date 2022-02-24
 */
public class CategoryUpdatedEvent extends ApplicationEvent {

    private final Category beforeUpdated;
    private final Category category;

    public CategoryUpdatedEvent(Object source, Category category, Category beforeUpdated) {
        super(source);
        this.category = category;
        this.beforeUpdated = beforeUpdated;
    }

    @Nullable
    public Category getCategory() {
        return category;
    }

    @Nullable
    public Category getBeforeUpdated() {
        return beforeUpdated;
    }
}
