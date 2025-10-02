package run.halo.app.core.attachment;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.core.extension.attachment.Attachment;

/**
 * Event triggered when an attachment is created, updated, or deleted.
 *
 * @author johnniang
 */
public class AttachmentChangedEvent extends ApplicationEvent {

    @Getter
    private final Attachment attachment;

    public AttachmentChangedEvent(Object source, Attachment attachment) {
        super(source);
        this.attachment = attachment;
    }

}
