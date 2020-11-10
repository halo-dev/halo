package run.halo.app.service;

import java.util.concurrent.Future;

public interface AttachmentHandlerCovertService {

    /**
     * Upload all attachments to the current Handler and update the attachment links in all posts,
     * If the upload is successful, the old attachment will be deleted, otherwise it will be kept.
     *
     * <p>
     * if uploadAll = true, Will try to download all the pictures in the post and upload them to the handler.
     * if uploadAll = false (default), Only upload pictures that already exist in the attachments.
     *
     * @param uploadAll default = false
     * @return AsyncResult<String>
     */
    Future<String> covertAttachmentHandler(Boolean uploadAll);
}
