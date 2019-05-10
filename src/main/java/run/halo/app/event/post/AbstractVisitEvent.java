package run.halo.app.event.post;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * Visit event.
 *
 * @author johnniang
 * @date 19-4-22
 */
public abstract class AbstractVisitEvent extends ApplicationEvent {

    private final Integer id;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param id     id
     */
    public AbstractVisitEvent(@NonNull Object source, @NonNull Integer id) {
        super(source);

        Assert.notNull(id, "Id must not be null");
        this.id = id;
    }

    @NonNull
    public Integer getId() {
        return id;
    }
}
