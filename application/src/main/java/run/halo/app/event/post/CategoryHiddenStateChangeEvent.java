package run.halo.app.event.post;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.core.extension.content.Category;

/**
 * When the category {@link Category.CategorySpec#isHideFromList()} state changes, this event is
 * triggered.
 *
 * @author guqing
 * @since 2.17.0
 */
@Getter
public class CategoryHiddenStateChangeEvent extends ApplicationEvent {
    private final String categoryName;
    private final boolean hidden;

    public CategoryHiddenStateChangeEvent(Object source, String categoryName, boolean hidden) {
        super(source);
        this.categoryName = categoryName;
        this.hidden = hidden;
    }
}
