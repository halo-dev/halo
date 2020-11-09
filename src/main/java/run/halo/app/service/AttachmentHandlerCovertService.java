package run.halo.app.service;

/**
 * Attachment Handler Covert Service.
 *
 * @author xcp
 * @date 2020-11-07
 */
public interface AttachmentHandlerCovertService {


    /**
     * Upload all attachments to the current Handler according to the attachment list.
     * Also update the attachment link in post.
     */
    void covertByAttachment();

    /**
     * Follow the link in Post to download the attachment and upload it to the current Handler.
     * Also update the attachment link in post.
     */
    void covertByPost();
}
