package run.halo.app.event.extension;

import org.springframework.context.ApplicationEvent;
import run.halo.app.extension.Extension;

/**
 * <p>Events produced during changing an {@link Extension}.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
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
