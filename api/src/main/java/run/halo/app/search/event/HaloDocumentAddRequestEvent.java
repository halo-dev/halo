package run.halo.app.search.event;

import org.springframework.context.ApplicationEvent;
import run.halo.app.plugin.SharedEvent;
import run.halo.app.search.HaloDocument;

@SharedEvent
public class HaloDocumentAddRequestEvent extends ApplicationEvent {

    private final Iterable<HaloDocument> documents;

    public HaloDocumentAddRequestEvent(Object source, Iterable<HaloDocument> documents) {
        super(source);
        this.documents = documents;
    }

    public Iterable<HaloDocument> getDocuments() {
        return documents;
    }

}
