package run.halo.app.search.event;

import org.springframework.context.ApplicationEvent;
import run.halo.app.plugin.SharedEvent;

@SharedEvent
public class HaloDocumentRebuildRequestEvent extends ApplicationEvent {

    public HaloDocumentRebuildRequestEvent(Object source) {
        super(source);
    }

}
