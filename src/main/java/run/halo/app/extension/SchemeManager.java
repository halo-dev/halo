package run.halo.app.extension;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.lang.NonNull;
import run.halo.app.extension.exception.SchemeNotFoundException;

public interface SchemeManager {

    void register(@NonNull Scheme scheme);

    /**
     * Registers an Extension using its type.
     *
     * @param type is Extension type.
     * @param <T> Extension class.
     */
    default <T extends Extension> void register(Class<T> type) {
        register(Scheme.buildFromType(type));
    }


    void unregister(@NonNull Scheme scheme);

    default int size() {
        return schemes().size();
    }

    @NonNull
    List<Scheme> schemes();

    @NonNull
    default Optional<Scheme> fetch(@NonNull GroupVersionKind gvk) {
        return schemes().stream()
            .filter(scheme -> Objects.equals(scheme.groupVersionKind(), gvk))
            .findFirst();
    }

    @NonNull
    default Scheme get(@NonNull GroupVersionKind gvk) {
        return fetch(gvk).orElseThrow(
            () -> new SchemeNotFoundException(gvk));
    }

    @NonNull
    default Scheme get(Class<? extends Extension> type) {
        var gvk = Scheme.getGvkFromType(type);
        return get(new GroupVersionKind(gvk.group(), gvk.version(), gvk.kind()));
    }

    @NonNull
    default Scheme get(Extension ext) {
        var gvk = ext.groupVersionKind();
        return get(gvk);
    }

}
