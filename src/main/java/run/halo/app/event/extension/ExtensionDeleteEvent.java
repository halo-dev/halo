package run.halo.app.event.extension;

import run.halo.app.extension.Extension;

/**
 * <p>Events produced during deleting an {@link Extension}.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
public class ExtensionDeleteEvent extends AbstractExtensionEvent {

    public ExtensionDeleteEvent(Object source, Extension extension) {
        super(source, extension);
    }
}
