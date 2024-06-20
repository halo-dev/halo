package run.halo.app.plugin;

import java.util.Objects;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * The event that delegates a shared event in core into all started plugins.
 *
 * @author johnniang
 * @since 2.17
 */
@Getter
class HaloSharedEventDelegator extends ApplicationEvent {

    private final ApplicationEvent delegate;

    public HaloSharedEventDelegator(Object source, ApplicationEvent delegate) {
        super(source);
        this.delegate = delegate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HaloSharedEventDelegator that = (HaloSharedEventDelegator) o;
        return Objects.equals(delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(delegate);
    }
}
