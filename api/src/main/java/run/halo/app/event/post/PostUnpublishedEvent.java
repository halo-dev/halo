package run.halo.app.event.post;

import run.halo.app.plugin.SharedEvent;

@SharedEvent
public class PostUnpublishedEvent extends PostEvent {

    public PostUnpublishedEvent(Object source, String postName) {
        super(source, postName);
    }

}
