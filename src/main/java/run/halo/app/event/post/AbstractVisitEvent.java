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

    private final String requestIp;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param requestIp the ip address of visitor, could be null (index)
     * @param id     id
     */
    public AbstractVisitEvent(@NonNull Object source, String requestIp, @NonNull Integer id) {
        super(source);

        Assert.notNull(id, "Id must not be null");
        this.id = id;

        this.requestIp = requestIp;
    }

    @NonNull
    public Integer getId() {
        return id;
    }

    public String getIp() {
        return requestIp;
    }
}
