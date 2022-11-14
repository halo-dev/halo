package run.halo.app.event.extension;

import run.halo.app.extension.Extension;

/**
 * <p>Events produced during adding an {@link Extension}.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
public class ExtensionAddEvent extends AbstractExtensionEvent {

    public ExtensionAddEvent(Object source, Extension extension) {
        super(source, extension);
    }
}
