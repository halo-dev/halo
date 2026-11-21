package run.halo.app.localization;

import java.util.Objects;
import org.springframework.context.ApplicationEvent;
import run.halo.app.plugin.SharedEvent;

/**
 * Explicit business notification for a non-Extension source, published by its owner after committing the change.
 * Consumers reload the current source through its adapter before acting; the event carries no mutable content and
 * grants no access. Halo broadcasts this shared event to other started plugins without intercepting database writes.
 *
 * @since 2.28.0
 */
@SharedEvent
public final class LocalizableResourceChangedEvent extends ApplicationEvent {
    private final ResourceReference subject;
    private final Change change;

    /**
     * Creates a committed business notification.
     *
     * @param subject stable external source identity
     * @param change business transition that occurred
     */
    public LocalizableResourceChangedEvent(ResourceReference subject, Change change) {
        super(Objects.requireNonNull(subject));
        this.subject = subject;
        this.change = Objects.requireNonNull(change);
        if (subject.isExtension()) {
            throw new IllegalArgumentException("Extension changes use their existing lifecycle events");
        }
    }

    /** Stable external source identity. */
    public ResourceReference subject() {
        return subject;
    }

    /** Explicit business transition. */
    public Change change() {
        return change;
    }

    /** Explicit committed business transitions, not inferred from database operations. */
    public enum Change {
        /** The source became publicly readable. */
        PUBLISHED,
        /** An already public source changed. */
        UPDATED,
        /** The source was deleted or became unavailable to public readers. */
        DELETED
    }
}
