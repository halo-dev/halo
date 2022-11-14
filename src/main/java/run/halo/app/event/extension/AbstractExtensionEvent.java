package run.halo.app.event.extension;

import org.springframework.context.ApplicationEvent;
import run.halo.app.extension.Extension;
import run.halo.app.plugin.SharedEvent;

/**
 * <p>Events produced during changing an {@link Extension}.</p>
 * This event is marked with {@link SharedEvent} annotation and will be propagated to the plugin
 * when the event is triggered.
 *
 * @author guqing
 * @since 2.0.0
 */
@SharedEvent
public abstract class AbstractExtensionEvent extends ApplicationEvent {

    private final Extension extension;

    public AbstractExtensionEvent(Object source, Extension extension) {
        super(source);
        this.extension = extension;
    }

    public Extension getExtension() {
        return extension;
    }
}
