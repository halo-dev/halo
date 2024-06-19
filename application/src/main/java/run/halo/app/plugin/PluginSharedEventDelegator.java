package run.halo.app.plugin;

import java.util.Objects;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;

/**
 * The event that delegates to another shared event published by a plugin.
 *
 * @author johnniang
 * @since 2.17
 */
@Getter
class PluginSharedEventDelegator extends ApplicationEvent {

    /**
     * The delegate event.
     */
    private final ApplicationEvent delegate;

    public PluginSharedEventDelegator(@NonNull Object source, @NonNull ApplicationEvent delegate) {
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

        PluginSharedEventDelegator that = (PluginSharedEventDelegator) o;
        return Objects.equals(delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(delegate);
    }
}
