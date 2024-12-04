package run.halo.app.plugin.extensionpoint;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;

@RequiredArgsConstructor
abstract class AbstractDefinitionGetter<E extends Extension>
    implements Reconciler<Reconciler.Request>, DisposableBean {

    protected final ConcurrentMap<String, E> cache = new ConcurrentHashMap<>();

    private final ExtensionClient client;

    private final E watchType;

    abstract void putCache(E definition);

    @Override
    @SuppressWarnings("unchecked")
    public Result reconcile(Request request) {
        client.fetch((Class<E>) watchType.getClass(), request.name())
            .ifPresent(this::putCache);
        return Result.doNotRetry();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder.extension(watchType)
            .syncAllOnStart(true)
            .build();
    }

    @Override
    public void destroy() throws Exception {
        cache.clear();
    }
}
