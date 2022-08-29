package run.halo.app.theme.router;

import run.halo.app.extension.AbstractExtension;

/**
 * Accept permalink change event.
 *
 * @param <T> extension type
 * @author guqing
 * @since 2.0.0
 */
public interface PermalinkWatch<T extends AbstractExtension> {

    void onPermalinkAdd(T extension);

    void onPermalinkUpdate(T extension);

    void onPermalinkDelete(T extension);
}
