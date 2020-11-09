package run.halo.app.service;

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
