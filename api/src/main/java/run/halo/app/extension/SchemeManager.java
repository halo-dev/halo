package run.halo.app.extension;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import run.halo.app.extension.exception.SchemeNotFoundException;
import run.halo.app.extension.index.IndexSpecs;

public interface SchemeManager {

    /**
     * Registers an Extension using its type.
     *
     * @param type is Extension type.
     * @param <E> Extension class.
     */
    default <E extends Extension> void register(Class<E> type) {
        register(type, null);
    }

    default void register(Scheme scheme) {
        register(scheme.type(), null);
    }

    <E extends Extension> void register(
        Class<E> type, @Nullable Consumer<IndexSpecs<E>> specsConsumer
    );

    void unregister(Scheme scheme);

    default int size() {
        return schemes().size();
    }

    List<Scheme> schemes();

    default Optional<Scheme> fetch(GroupVersionKind gvk) {
        return schemes().stream()
            .filter(scheme -> Objects.equals(scheme.groupVersionKind(), gvk))
            .findFirst();
    }

    default Scheme get(GroupVersionKind gvk) {
        return fetch(gvk).orElseThrow(
            () -> new SchemeNotFoundException(gvk));
    }

    default Scheme get(Class<? extends Extension> type) {
        var gvk = Scheme.getGvkFromType(type);
        return get(new GroupVersionKind(gvk.group(), gvk.version(), gvk.kind()));
    }

    default Scheme get(Extension ext) {
        var gvk = ext.groupVersionKind();
        return get(gvk);
    }

}
