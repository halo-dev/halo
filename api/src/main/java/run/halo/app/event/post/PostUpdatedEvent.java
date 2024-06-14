package run.halo.app.event.post;

import run.halo.app.plugin.SharedEvent;

@SharedEvent
public class PostUpdatedEvent extends PostEvent {

    public PostUpdatedEvent(Object source, String postName) {
        super(source, postName);
    }

}
