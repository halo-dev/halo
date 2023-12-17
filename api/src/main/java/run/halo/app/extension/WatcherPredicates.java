package run.halo.app.extension;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class WatcherPredicates {

    static final Predicate<Extension> EMPTY_PREDICATE = (e) -> true;

    static final BiPredicate<Extension, Extension> EMPTY_BI_PREDICATE = (oldExt, newExt) -> true;
    private final Predicate<Extension> onAddPredicate;
    private final BiPredicate<Extension, Extension> onUpdatePredicate;
    private final Predicate<Extension> onDeletePredicate;

    public WatcherPredicates(Predicate<Extension> onAddPredicate,
        BiPredicate<Extension, Extension> onUpdatePredicate,
        Predicate<Extension> onDeletePredicate) {
        this.onAddPredicate = onAddPredicate;
        this.onUpdatePredicate = onUpdatePredicate;
        this.onDeletePredicate = onDeletePredicate;
    }

    public Predicate<Extension> onAddPredicate() {
        if (onAddPredicate == null) {
            return EMPTY_PREDICATE;
        }
        return onAddPredicate;
    }

    public BiPredicate<Extension, Extension> onUpdatePredicate() {
        if (onUpdatePredicate == null) {
            return EMPTY_BI_PREDICATE;
        }
        return onUpdatePredicate;
    }

    public Predicate<Extension> onDeletePredicate() {
        if (onDeletePredicate == null) {
            return EMPTY_PREDICATE;
        }
        return onDeletePredicate;
    }

    public static final class Builder {

        private Predicate<Extension> onAddPredicate;
        private BiPredicate<Extension, Extension> onUpdatePredicate;
        private Predicate<Extension> onDeletePredicate;

        private GroupVersionKind gvk;

        public Builder withGroupVersionKind(GroupVersionKind gvk) {
            this.gvk = gvk;
            return this;
        }

        public Builder onAddPredicate(Predicate<Extension> onAddPredicate) {
            this.onAddPredicate = onAddPredicate;
            return this;
        }

        public Builder onUpdatePredicate(
            BiPredicate<Extension, Extension> onUpdatePredicate) {
            this.onUpdatePredicate = onUpdatePredicate;
            return this;
        }

        public Builder onDeletePredicate(Predicate<Extension> onDeletePredicate) {
            this.onDeletePredicate = onDeletePredicate;
            return this;
        }

        public WatcherPredicates build() {
            Predicate<Extension> gvkPredicate = EMPTY_PREDICATE;
            BiPredicate<Extension, Extension> gvkBiPredicate = EMPTY_BI_PREDICATE;
            if (gvk != null) {
                gvkPredicate = e -> gvk.equals(e.groupVersionKind());
                gvkBiPredicate = (oldE, newE) -> oldE.groupVersionKind().equals(gvk)
                    && newE.groupVersionKind().equals(gvk);
            }
            if (onAddPredicate == null) {
                onAddPredicate = EMPTY_PREDICATE;
            }
            if (onUpdatePredicate == null) {
                onUpdatePredicate = EMPTY_BI_PREDICATE;
            }
            if (onDeletePredicate == null) {
                onDeletePredicate = EMPTY_PREDICATE;
            }

            onAddPredicate = gvkPredicate.and(onAddPredicate);
            onUpdatePredicate = gvkBiPredicate.and(onUpdatePredicate);
            onDeletePredicate = gvkPredicate.and(onDeletePredicate);

            return new WatcherPredicates(onAddPredicate, onUpdatePredicate, onDeletePredicate);
        }

    }
}
