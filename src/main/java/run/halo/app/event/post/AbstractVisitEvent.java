package run.halo.app.event.post;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Visit event.
 *
 * @author johnniang
 * @date 19-4-22
 */
public abstract class AbstractVisitEvent extends ApplicationEvent {

    private final Integer id;

    private final String requestIp;

    /**
     * Create a new ApplicationEvent (with ip).
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param requestIp the ip address of visitor, could be null (index)
     * @param id     id of post/sheet
     */
    public AbstractVisitEvent(@NonNull Object source, @Nullable String requestIp, @NonNull Integer id) {
        super(source);

        Assert.notNull(id, "Id must not be null");
        this.id = id;

        this.requestIp = requestIp;
    }

    /**
     * Create a new ApplicationEvent (without ip).
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param id     id of post/sheet
     */
    public AbstractVisitEvent(@NonNull Object source, @NonNull Integer id) {
        super(source);

        Assert.notNull(id, "Id must not be null");
        this.id = id;

        this.requestIp = null;
    }

    @NonNull
    public Integer getId() {
        return id;
    }

    @Nullable
    public String getIp() {
        return requestIp;
    }
}
