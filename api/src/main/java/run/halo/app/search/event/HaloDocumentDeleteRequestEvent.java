package run.halo.app.search.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.Nullable;
import run.halo.app.plugin.SharedEvent;

@SharedEvent
public class HaloDocumentDeleteRequestEvent extends ApplicationEvent {

    private final Iterable<String> docIds;

    /**
     * Construct a new {@code HaloDocumentDeleteRequestEvent} instance.
     *
     * @param source The source of the event.
     * @param docIds If the document IDs are not provided, all documents will be deleted.
     */
    public HaloDocumentDeleteRequestEvent(Object source, @Nullable Iterable<String> docIds) {
        super(source);
        this.docIds = docIds;
    }

    public Iterable<String> getDocIds() {
        return docIds;
    }

}
