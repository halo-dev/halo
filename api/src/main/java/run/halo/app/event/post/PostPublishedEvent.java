package run.halo.app.event.post;

import run.halo.app.plugin.SharedEvent;

@SharedEvent
public class PostPublishedEvent extends PostEvent {

    public PostPublishedEvent(Object source, String postName) {
        super(source, postName);
    }

}
