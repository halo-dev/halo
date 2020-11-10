package run.halo.app.service;

import java.util.concurrent.Future;

public interface AttachmentHandlerCovertService {

    /**
     * Upload attachments to the current Handler and update the attachment link in post.
     * <p>
     * if uploadAll = true, Will try to download all the pictures in the post and upload them to the handler.
     * if uploadAll = false, Only upload pictures that already exist in the attachment.
     *
     * @param uploadAll default = false
     * @return AsyncResult<String>
     */
    Future<String> covertAttachmentHandler(Boolean uploadAll);

}
