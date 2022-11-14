package run.halo.app.event.extension;

import run.halo.app.extension.Extension;

/**
 * <p>Events produced during updating an {@link Extension}.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
public class ExtensionUpdateEvent extends AbstractExtensionEvent {
    private final Extension oldExtension;

    public ExtensionUpdateEvent(Object source, Extension oldExtension, Extension extension) {
        super(source, extension);
        this.oldExtension = oldExtension;
    }

    public Extension getOldExtension() {
        return oldExtension;
    }
}
