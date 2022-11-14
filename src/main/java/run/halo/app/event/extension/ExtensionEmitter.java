package run.halo.app.event.extension;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Watcher;

/**
 * <p>An emitter to emit events produced during changing an {@link Extension}.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ExtensionEmitter implements Watcher, InitializingBean {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ReactiveExtensionClient client;

    private volatile boolean disposed = false;

    private Runnable disposeHook;

    public ExtensionEmitter(ApplicationEventPublisher applicationEventPublisher,
        ReactiveExtensionClient client) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.client = client;
    }

    @Override
    public void onAdd(Extension extension) {
        if (isDisposed()) {
            return;
        }
        applicationEventPublisher.publishEvent(new ExtensionAddEvent(this, extension));
    }

    @Override
    public void onUpdate(Extension oldExtension, Extension newExtension) {
        if (isDisposed()) {
            return;
        }
        applicationEventPublisher.publishEvent(
            new ExtensionUpdateEvent(this, oldExtension, newExtension));
    }

    @Override
    public void onDelete(Extension extension) {
        if (isDisposed()) {
            return;
        }
        applicationEventPublisher.publishEvent(new ExtensionDeleteEvent(this, extension));
    }

    @Override
    public void registerDisposeHook(Runnable dispose) {
        this.disposeHook = dispose;
    }

    @Override
    public void dispose() {
        this.disposed = true;
        if (this.disposeHook != null) {
            this.disposeHook.run();
        }
    }

    @Override
    public boolean isDisposed() {
        return this.disposed;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        client.watch(this);
    }
}
